package com.springboot.stackoverflow.services;

import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.entity.Tag;
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

    void votingSystem(Integer vote, String type, Integer questionId, Integer answerId);

    void saveCommentList(Question question);
    void bookmarkQuestion(int questionId);

    void removeBookmarkQuestion(int questionId);
    void acceptAnswer(Integer questionId, Integer answerId);

    List<Question> searchProducts(String search);
    public List<Object> getSortedCommentsAndAnswers(Integer question_id);
}
