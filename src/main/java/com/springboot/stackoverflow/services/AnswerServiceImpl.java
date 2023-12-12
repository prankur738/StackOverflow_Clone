package com.springboot.stackoverflow.services;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.springboot.stackoverflow.entity.Answer;
import com.springboot.stackoverflow.entity.Badge;
import com.springboot.stackoverflow.entity.Question;
import com.springboot.stackoverflow.entity.User;
import com.springboot.stackoverflow.repository.AnswerRepository;
import com.springboot.stackoverflow.repository.QuestionRepository;
import com.springboot.stackoverflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

@Service
public class AnswerServiceImpl implements AnswerService{

    QuestionRepository questionRepository;
    AnswerRepository answerRepository;
    UserRepository userRepository;
    BadgeService badgeService;

    @Autowired
    public AnswerServiceImpl(QuestionRepository questionRepository,AnswerRepository answerRepository
                            ,UserRepository userRepository,BadgeService badgeService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.badgeService=badgeService;
    }

    @Override
    public void addAnswer(Integer questionId, String content, MultipartFile file) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName());
        Optional<Question> questionRetrievedByID = questionRepository.findById(questionId);
        if(questionRetrievedByID.isPresent()){
            Question question = questionRetrievedByID.get();
            Answer answer = new Answer(content, question, null);

            answer.setAuthor(user.getUserName());
            answer.setUser(user);
            answer.setCreatedAt(new Date());

            if(!file.isEmpty()){
                String fileName = file.getOriginalFilename();
                java.io.File tempFile = java.io.File.createTempFile("temp", null);
                file.transferTo(tempFile.toPath()); // transfer data to io.File

                // Accessing serviceAccount file for firebase Authentication
                try (FileInputStream serviceAccount = new FileInputStream("./serviceAccountKey.json")) {
                    //Creates a BlobId and BlobInfo for the file in firebase Storage.
                    //BlobId is unique identifier of Blob object in firebase
                    BlobId blobId = BlobId.of("stack-overflow-clone-857f4.appspot.com", fileName);
                    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();

                    //reading credentials from serviceAccount
                    //uploading file content from temporary file to firebase
                    Credentials credentials = GoogleCredentials.fromStream(serviceAccount);
                    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                    storage.create(blobInfo, Files.readAllBytes(tempFile.toPath()));

                    String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/drive-db-415a1/o/%s?alt=media";

                    //saving metadata in file entity

                    answer.setPhotoName(file.getOriginalFilename());
                    answer.setPhotoLink(String.format(DOWNLOAD_URL, URLEncoder.encode(fileName, StandardCharsets.UTF_8)));
                    answer.setPhotoSize(file.getSize());  // Set the file size
                    answer.setPhotoType(file.getContentType());


                } catch (IOException e) {
                    // Handle the exception (log, throw, or return an error response)
                    throw new RuntimeException("Error uploading file to Firebase Storage", e);
                } finally {
                    // Delete the temporary file
                    tempFile.delete();
                }

            }
            user.setReputation(user.getReputation()+5);
            badgeService.checkAndAssignBadges(user.getId());
            userRepository.save(user);
            answerRepository.save(answer);
        }
    }

    @Override
    public Answer editAnswer(Integer answerId) {
        Optional<Answer> retrievedAnswerById = answerRepository.findById(answerId);
        Answer tempAnswer = null;
        if(retrievedAnswerById.isPresent()){
            tempAnswer = retrievedAnswerById.get();
        }
        return tempAnswer;
    }

    @Override
    public void updateAnswer(String updatedAnswer, Integer answerId) {
        Answer answer = answerRepository.findById(answerId).get();
        answer.setContent(updatedAnswer);
        answerRepository.save(answer);
    }

    @Override
    public void deleteAnswer(Integer answerId) {
            answerRepository.deleteById(answerId);
    }

    @Override
    public Answer findAnswerById(int answerId) {
        return answerRepository.findById(answerId).orElse(null);
    }

    @Override
    public void saveCommentList(Answer answer) {
        answerRepository.save(answer);
    }
}
