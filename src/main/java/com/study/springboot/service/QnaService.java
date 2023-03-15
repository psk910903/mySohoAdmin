package com.study.springboot.service;

import com.study.springboot.dto.qna.QnaResponseDto;
import com.study.springboot.dto.qna.QnaSaveDto;
import com.study.springboot.entity.QnaCommentEntity;
import com.study.springboot.entity.QnaEntity;
import com.study.springboot.entity.repository.QnaCommentRepository;
import com.study.springboot.entity.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;

    public boolean save(QnaEntity qna) {
        try {
            qnaRepository.save(qna);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    public Page<QnaResponseDto> findAll(int page) {

        // 정렬기능 추가
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("qnaId"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        Page<QnaEntity> list = qnaRepository.findAll(pageable);

        return list.map(QnaResponseDto::new);
    }


    public List<QnaResponseDto> findbyid(long id) {
        Optional<QnaEntity> list = qnaRepository.findById(id);
        return list.stream().map(QnaResponseDto::new).collect(Collectors.toList());
    }

    public boolean delete(Long id) {

        Optional<QnaEntity> byId = qnaRepository.findById(id);
        List<QnaCommentEntity> QnaCommentList = qnaCommentRepository.findByCommentQnaId_nativeQuery(id);
        if (!byId.isPresent()) {
            return false;
        }
        QnaEntity qnaEntity = byId.get();
        try {
            qnaRepository.delete(qnaEntity);
            for (QnaCommentEntity temp : QnaCommentList) {
                qnaCommentRepository.delete(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    @Transactional
//    public void modifyHits(long id) {
//        qnaRepository.modifyHits(id);
//    }

    public List<Integer> getPageList(final int totalPage, final int page) {

        List<Integer> pageList = new ArrayList<>();
        // 페이지 숫자
        if (totalPage <= 5) {
            for (Integer i = 0; i <= totalPage - 1; i++) {
                pageList.add(i);
            }
        } else if (page >= 0 && page <= 2) {
            for (Integer i = 0; i <= 4; i++) {
                pageList.add(i);
            }
        } else if (page >= totalPage - 3 && page <= totalPage - 1) {
            for (Integer i = 5; i >= 1; i--) {
                pageList.add(totalPage - i);
            }
        } else {
            for (Integer i = -2; i <= 2; i++) {
                pageList.add(page + i);
            }
        }
        return pageList;
    }
    @Transactional(readOnly = true)
    public Page<QnaResponseDto> findByKeyword(String keywordType, String keyword, int page) {
        //정렬
        Page<QnaEntity> list = null;
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("qnaId"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));


        if (keywordType.equals("title")) {
            list = qnaRepository.findByQnaContentContaining(keyword, pageable);
        } else if (keywordType.equals("writer")) {
            list = qnaRepository.findByQnaNameContaining(keyword, pageable);
        } else {
            list = qnaRepository.findByQnaCategoryContaining(keyword, pageable);
        }
        return list.map(QnaResponseDto::new);
    }


    @Transactional(readOnly = true)
    public Page<QnaResponseDto> findByDate(String dateStart, String dateEnd, int page) throws ParseException {

        Page<QnaEntity> list;
        Pageable pageable = PageRequest.of(page, 10);

        //문자열을 날짜형식으로 변환
        DateFormat sdFormatStart = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat sdFormatEnd = new SimpleDateFormat("yyyy-MM-dd");
        Date tempDateStart = sdFormatStart.parse(dateStart);
        Date tempDateEnd = sdFormatEnd.parse(dateEnd);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(tempDateStart);
        cal2.setTime(tempDateEnd);
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        cal2.add(Calendar.DATE, +1);

        //날짜형식을 문자열로 변환
        String dateStartStr = df1.format(cal1.getTime()) + " 00:00:00";
        String dateEndStr = df2.format(cal2.getTime()) + " 00:00:00";

        list = qnaRepository.findByQnaLocalDateTimeContaining(dateStartStr, dateEndStr, pageable);
        return list.map(QnaResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<QnaResponseDto> findByDate(String dateStart, int page) throws ParseException {

        //오늘날짜로 date객체 2개 생성 (~부터 ~까지로 검색에 사용목적)
        Date tempDateStart = new Date();
        Date tempDateEnd = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(tempDateStart);
        cal2.setTime(tempDateEnd);
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

        //매개변수로 들어온 모드 확인 후 검색기간 수정
        if (dateStart.equals("today")) {
            cal2.add(Calendar.DATE, +1);
        } else if (dateStart.equals("yesterday")) {
            cal1.add(Calendar.DATE, -1);
            cal2.add(Calendar.DATE, +1);
        } else if (dateStart.equals("week")) {
            cal1.add(Calendar.DATE, -7);
            cal2.add(Calendar.DATE, +1);
        } else if (dateStart.equals("month")) {
            cal1.add(Calendar.MONTH, -1);
            cal2.add(Calendar.DATE, +1);
        }

        String dateStartStr = df1.format(cal1.getTime());
        String dateEndStr = df2.format(cal2.getTime());

        return findByDate(dateStartStr, dateEndStr, page);
    }
    @Transactional
    public boolean selectDelete(final String qnaNo) {
        try {
            String[] arrIdx = qnaNo.split(",");
            for (int i=0; i<arrIdx.length; i++) {
                QnaEntity qnaEntity = qnaRepository.findById((long) Integer.parseInt(arrIdx[i])).get();
                qnaRepository.delete(qnaEntity);
                List<QnaCommentEntity> list = qnaCommentRepository.findByCommentQnaId_nativeQuery((long) Integer.parseInt(arrIdx[i]));
                qnaCommentRepository.deleteAll(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //qna 아이디 마스킹
    public List<String> qnaMaskingId(List<QnaResponseDto> list) {
        List<String> nameList = new ArrayList<>();
        for(int i=0 ; i < list.size();i++){

            String qnaName = list.get(i).getMemberId();

            if(qnaName == null){
                qnaName = list.get(i).getQnaName();

            }
            String qnaHiddenName;
            if (qnaName.length() == 2){
                qnaHiddenName = qnaName.replace(qnaName.charAt(1), '*');
            }else if(qnaName.length() == 1){
                qnaHiddenName = qnaName;
            }
            else{
                qnaHiddenName = qnaName.substring(0,2);
                //
                for (int j=0; j<qnaName.length()-2; j++){
                    qnaHiddenName += "*";
                }
            }
            nameList.add(qnaHiddenName);
        }
        return nameList;
    }
    // 나의 문의 내역 (qna 문의) ----------------------------------------
    @Transactional(readOnly = true)
    public List<QnaResponseDto> findByMemberIdQna(String memberId) { // 이름 중복됨 나중에 service 옮길때 함수 명 변경해야함
        List<QnaEntity> list = qnaRepository.findByMemberId(memberId);
        return list.stream().map(QnaResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countByQnaId(Long qnaId) { // 이름 중복됨 나중에 service 옮길때 함수 명 변경해야함
        Long count = qnaCommentRepository.countByQnaId(qnaId);
        return count;
    }

    public List<QnaResponseDto> findAll() {
        Sort sort = Sort.by(Sort.Direction.DESC, "qnaId");
        List<QnaEntity> list =qnaRepository.findAll(sort);
        return list.stream().map(QnaResponseDto::new).collect(Collectors.toList());
    }
    @Transactional
    public List<QnaResponseDto> findByKeyword(String keyword) {

        List<QnaEntity> list =qnaRepository.findByQnaContentContaining(keyword);
        return list.stream().map(QnaResponseDto::new).collect(Collectors.toList());
    }
    @Transactional
    public boolean qnaSave(QnaEntity qnaEntity) {
        try{
            qnaRepository.save(qnaEntity);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Transactional
    public boolean pwCheck(QnaSaveDto qnaSaveDto) {
        // 글번호로 DB에서 조회를함
        Optional<QnaEntity> byId = qnaRepository.findById(qnaSaveDto.getQnaId());
        if(byId.isPresent()){// 글번호가 있다.
            QnaEntity qnaEntity = byId.get();
            if(qnaEntity.getQnaPassword().equals(qnaSaveDto.getQnaPassword())){//패스워드 일치
                return true;
            }else{// 패스워드 불일치
                return false;
            }
        }else{// 글번호가 없다.
            return false;
        }
        // DB에서 조회한 비밀번호와 사용자가 입력한 비밀번호가 일치하는지 판단
    }

    @Transactional
    public QnaResponseDto findById(Long num) {
        Optional<QnaEntity> qnaEntity = qnaRepository.findById(num);
        return new QnaResponseDto(qnaEntity.get());
    }

}
