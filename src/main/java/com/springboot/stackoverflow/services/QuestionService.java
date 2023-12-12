package com.springboot.stackoverflow.services;

import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface QuestionService {
    void saveQuestion(Question newQuestion, Tag newTag, MultipartFile file) throws IOException;

    Question editQuestion(Integer questionId);

    void updateQuestion(Integer questionId, String editedContent);

    void deleteQuestion(Integer questionId);

    Question findQuestionById(Integer questionId);

    List<Question> findQuestionsList();

    Page<Question> findAllQuestions(int pageNumber, int pageLimit);

    void votingSystem(Integer vote, String type, Integer questionId, Integer answerId);

    void saveCommentList(Question question);
    void bookmarkQuestion(int questionId);

    void removeBookmarkQuestion(int questionId);
    void acceptAnswer(Integer questionId, Integer answerId);



    public List<Object> getSortedCommentsAndAnswers(Integer question_id);

    Page<Question> searchProducts(String search, int pageNumber, int pageLimit);

}
