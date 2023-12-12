package com.springboot.stackoverflow.services;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.springboot.stackoverflow.entity.*;
import com.springboot.stackoverflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuestionServiceImpl implements QuestionService{
   TagService tagService;
   TagRepository tagRepository;
   QuestionRepository questionRepository;
   UserRepository userRepository;
   AnswerRepository answerRepository;
   VoteRepository voteRepository;
   BadgeService badgeService;
    public QuestionServiceImpl(){}

    @Autowired
    public QuestionServiceImpl(TagService tagService,QuestionRepository questionRepository,
                               TagRepository tagRepository, UserRepository userRepository,
                               AnswerRepository answerRepository, VoteRepository voteRepository,
                               BadgeService badgeService) {
        this.tagService = tagService;
        this.questionRepository = questionRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.answerRepository = answerRepository;
        this.badgeService=badgeService;
        this.voteRepository = voteRepository;
    }

    @Override
    public void saveQuestion(Question newQuestion, Tag newTag, MultipartFile file) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName());
        newQuestion.setViews(0);
        newQuestion.setVotes(0);
        newQuestion.setAuthor(user.getUserName());
        newQuestion.setUser(user);

        Map<String,Tag> tempTags = new HashMap<>();
        List<Tag> allTags = tagService.findAllTags();

        for(Tag tag:allTags){
            String name = tag.getName();
            tempTags.put(name,tag);
        }

        String[] tagsArray = newTag.getName().split(",");
        for(String tempTag: tagsArray){
            tempTag = tempTag.trim();
            if(!tempTag.isEmpty())
            {
                if(tempTags.containsKey(tempTag)){
                    newQuestion.addTags(tempTags.get(tempTag));
                }
                else {
                    Tag tag = new Tag(tempTag);
                    newQuestion.addTags(tag);
                }
            }
        }


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

                newQuestion.setPhotoName(file.getOriginalFilename());
                newQuestion.setPhotoLink(String.format(DOWNLOAD_URL, URLEncoder.encode(fileName, StandardCharsets.UTF_8)));
                newQuestion.setPhotoSize(file.getSize());  // Set the file size
                newQuestion.setPhotoType(file.getContentType());


            } catch (IOException e) {
                // Handle the exception (log, throw, or return an error response)
                throw new RuntimeException("Error uploading file to Firebase Storage", e);
            } finally {
                // Delete the temporary file
                tempFile.delete();
            }

        }
        user.setReputation(user.getReputation()+20);
        badgeService.checkAndAssignBadges(user.getId());
        userRepository.save(user);
        questionRepository.save(newQuestion);
    }

    @Override
    public Question editQuestion(Integer questionId) {
        Optional<Question> questionRetrieved = questionRepository.findById(questionId);
        Question tempQuestion = null;
        if(questionRetrieved.isPresent()){
                tempQuestion  = questionRetrieved.get();
        }
        return tempQuestion;
    }


    @Override
    public void updateQuestion(Integer questionId, String editedContent) {
        Question updatedQuestion = questionRepository.findById(questionId).get();
        updatedQuestion.setContent(editedContent);

        questionRepository.save(updatedQuestion);
    }

    @Override
    public void deleteQuestion(Integer questionId) {
        Optional<Question> questionRetrievedById = questionRepository.findById(questionId);
        List<Tag> tags = null;
        if(questionRetrievedById.isPresent()){
            Question question = questionRetrievedById.get();
             tags = question.getTags();
        }
        questionRepository.deleteById(questionId);
        System.out.println(tags);
        if(tags != null){
            for(Tag tempTag : tags){
               if(tempTag.getQuestions().isEmpty()){
                   tagRepository.deleteById(tempTag.getId());
               }
            }
        }
    }

    @Override
    public Question findQuestionById(Integer questionId) {
        Optional<Question> byId = questionRepository.findById(questionId);
        if(byId.isEmpty()) return null;
        byId.get().setViews(byId.get().getViews()+1);
        questionRepository.save(byId.get());

        return byId.get();
    }

    @Override
    public List<Question> findQuestionsList() {
        return questionRepository.findAll();
    }

    @Override
    public Page<Question> findAllQuestions(int pageNumber, int pageLimit) {
        return questionRepository.findAll(PageRequest.of(pageNumber, pageLimit));
    }

    public void saveCommentList(Question question) {
        questionRepository.save(question);
    }

    @Override
    public void bookmarkQuestion(int questionId) {
        Optional<Question> retrievedQuestionById = questionRepository.findById(questionId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName());
        if(retrievedQuestionById.isPresent()){
            Question question = retrievedQuestionById.get();
            question.addSavedUser(user);
            questionRepository.save(question);
            user.setReputation(user.getReputation()+5);
            badgeService.checkAndAssignBadges(user.getId());
            userRepository.save(user);
            User questionUser = userRepository.findByEmail(question.getUser().getEmail());
            questionUser.setReputation(questionUser.getReputation()+10);
            badgeService.checkAndAssignBadges(questionUser.getId());

            userRepository.save(user);
        }
    }

    @Override
    public void removeBookmarkQuestion(int questionId) {
        Optional<Question> retrievedQuestionById = questionRepository.findById(questionId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName());
        if(retrievedQuestionById.isPresent()){
            Question question = retrievedQuestionById.get();
            question.removedSavedUser(user);
            questionRepository.save(question);
            user.setReputation(user.getReputation()-5);
            userRepository.save(user);
            User questionUser = userRepository.findByEmail(question.getUser().getEmail());
            questionUser.setReputation(questionUser.getReputation()-10);
            userRepository.save(user);
        }
    }

    @Override
    public void acceptAnswer(Integer questionId, Integer answerId) {
        Question question = questionRepository.findById(questionId).get();
        Answer answer = answerRepository.findById(answerId).get();

        if(question.getAcceptedAnswer() != null && question.getAcceptedAnswer().equals(answer)) {
            User user = userRepository.findByEmail(answer.getUser().getEmail());
            System.out.println(user);
            user.setReputation(user.getReputation()-25);
            userRepository.save(user);
            question.setAcceptedAnswer(null);

        }
        else
            question.setAcceptedAnswer(answer);
        User user = userRepository.findByEmail(answer.getUser().getEmail());
        System.out.println(user);
        user.setReputation(user.getReputation()+25);
        badgeService.checkAndAssignBadges(user.getId());
        userRepository.save(user);
        questionRepository.save(question);
    }



    @Override
    public Page<Question> searchProducts(String search, int pageNumber, int pageLimit) {
        return questionRepository.searchQuestionsPaginated(search, PageRequest.of(pageNumber, pageLimit));
    }

    @Override
    public void votingSystem(Integer vote, String type, Integer questionId, Integer answerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName());

        if (type.equals("question")) {
            if (vote.equals(1)) {
                for (Vote tempVote : user.getUserVote()) {
                    System.out.println("here");
                    if (tempVote.getQuestionId() != null && tempVote.getQuestionId().equals(questionId)) {
                        if (tempVote.getDirection().equals(1)) {
                            return;
                        } else if (tempVote.getDirection().equals(-1)) {
                            Question question = questionRepository.findById(questionId).get();
                            question.setVotes(question.getVotes() + 1);
                            user.getUserVote().remove(tempVote);

                            voteRepository.delete(tempVote);
                            questionRepository.save(question);
                            userRepository.save(user);
                            return;
                        } else {
                            break;
                        }
                    }
                }

                Vote theVote = new Vote(answerId, questionId, vote);
                Question question = questionRepository.findById(questionId).get();
                question.setVotes(question.getVotes() + 1);
                questionRepository.save(question);

                user.addVote(theVote);
                userRepository.save(user);
            } else if (vote.equals(-1)) {
                for (Vote tempVote : user.getUserVote()) {
                    if (tempVote.getQuestionId() != null && tempVote.getQuestionId().equals(questionId)) {
                        if (tempVote.getDirection().equals(-1)) {
                            return;
                        } else if (tempVote.getDirection().equals(1)) {
                            System.out.println("here2");
                            Question question = questionRepository.findById(questionId).get();
                            question.setVotes(question.getVotes() - 1);
                            user.getUserVote().remove(tempVote);

                            voteRepository.delete(tempVote);
                            questionRepository.save(question);
                            userRepository.save(user);
                            return;
                        } else {
                            break;
                        }
                    }
                }

                Vote theVote = new Vote(answerId, questionId, vote);
                Question question = questionRepository.findById(questionId).get();
                question.setVotes(question.getVotes() - 1);
                questionRepository.save(question);

                user.addVote(theVote);
                userRepository.save(user);
            }
        } else if (type.equals("answer")) {
            if (vote.equals(1)) {
                for (Vote tempVote : user.getUserVote()) {
                    if (tempVote.getAnswerId() != null && tempVote.getAnswerId().equals(answerId)) {
                        if (tempVote.getDirection().equals(1)) {
                            return;
                        } else if (tempVote.getDirection().equals(-1)) {
                            Answer answer = answerRepository.findById(answerId).get();
                            answer.setVotes(answer.getVotes() + 1);
                            user.getUserVote().remove(tempVote);

                            voteRepository.delete(tempVote);
                            answerRepository.save(answer);
                            userRepository.save(user);
                            return;
                        } else {
                            break;
                        }
                    }
                }

                Vote theVote = new Vote(answerId, null, vote);
                Answer answer = answerRepository.findById(answerId).get();
                answer.setVotes(answer.getVotes() + 1);
                answerRepository.save(answer);

                user.addVote(theVote);
                userRepository.save(user);
            } else if (vote.equals(-1)) {
                for (Vote tempVote : user.getUserVote()) {
                    if (tempVote.getAnswerId() != null && tempVote.getAnswerId().equals(answerId)) {
                        if (tempVote.getDirection().equals(-1)) {
                            return;
                        } else if (tempVote.getDirection().equals(1)) {
                            Answer answer = answerRepository.findById(answerId).get();
                            answer.setVotes(answer.getVotes() - 1);
                            user.getUserVote().remove(tempVote);

                            voteRepository.delete(tempVote);
                            answerRepository.save(answer);
                            userRepository.save(user);
                            return;
                        } else {
                            break;
                        }
                    }
                }

                Vote theVote = new Vote(answerId, null, vote);
                Answer answer = answerRepository.findById(answerId).get();
                answer.setVotes(answer.getVotes() - 1);
                answerRepository.save(answer);

                user.addVote(theVote);
                userRepository.save(user);
            }
        }
    }

//    public List<Object> getSortedCommentsAndAnswers(Integer question_id) {
//        System.out.println("Enter in func");
//        List<Object> sortedList = new ArrayList<>();
//        Question question = questionRepository.findById(question_id).get();
//
//        // Add comments to the list
//        if (question.getComments() != null) {
//            sortedList.addAll(question.getComments());
//        }
//
//        // Add answers to the list
//        if (question.getAnswers() != null) {
//            sortedList.addAll(question.getAnswers());
//        }
//
//        // Sort the list based on createdAt time
//        Collections.sort(sortedList, Comparator.comparing(item -> {
//            if (item instanceof Comment) {
//                return ((Comment) item).getCreatedAt();
//            } else if (item instanceof Answer) {
//                return ((Answer) item).getCreatedAt();
//            }
//            return null; // Handle other types or return appropriate default
//        }));
//        System.out.println("Exiting func");
//
//        return sortedList;
//    }

    public List<Object> getSortedCommentsAndAnswers(Integer question_id) {
        System.out.println("Enter in func");
        List<Object> sortedList = new ArrayList<>();

        Optional<Question> optionalQuestion = questionRepository.findById(question_id);

        // Check if the question is present
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();

            // Add comments to the list
            if (question.getComments() != null) {
                sortedList.addAll(question.getComments());
            }

            // Add answers to the list
            if (question.getAnswers() != null) {
                sortedList.addAll(question.getAnswers());
            }

            // Sort the list based on createdAt time
            Collections.sort(sortedList, Comparator.comparing(item -> {
                if (item instanceof Comment) {
                    return ((Comment) item).getUpdatedAt();
                } else if (item instanceof Answer) {
                    return ((Answer) item).getCreatedAt();
                }
                return null; // Handle other types or return appropriate default
            }));

            System.out.println("Exiting func");
        } else {
            System.out.println("Question not found for ID: " + question_id);
        }

        return sortedList;
    }

}

