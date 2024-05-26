package uz.shoh.tjbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.shoh.tjbot.entitys.test.TestRepository;
import uz.shoh.tjbot.entitys.test.entity.Test;
import uz.shoh.tjbot.entitys.testSubmission.TestSubmissionRepository;
import uz.shoh.tjbot.entitys.testSubmission.entity.TestSubmission;
import uz.shoh.tjbot.entitys.user.UserRepository;
import uz.shoh.tjbot.entitys.user.entiry.User;
import uz.shoh.tjbot.entitys.user.enums.UserState;
import uz.shoh.tjbot.message.MyMessageBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class UpdateHandler {
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final TestSubmissionRepository testSubmissionRepository;
    private final DefaultAbsSender sender;


    @SneakyThrows
    @Transactional
    public void handler(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            String userId = update.getCallbackQuery().getFrom().getId().toString();
            handleCallbackQuery(update, userId);
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        String chatId = update.getMessage().getChatId().toString();
        String text = update.getMessage().getText();

        User user = getUserById(chatId);
        if (user == null) {
            checkFullName(text, chatId);
        } else {
            boolean subscribeToChannel = subscribeToChannel(user.getId());
            if (!subscribeToChannel) {
                sender.execute(MyMessageBuilder.handleSubscribeActionMessage(user.getId(), false));
            } else {
                if (text.startsWith("/")) {
                    switch (text) {
                        case "/yordam" -> {
                            sender.execute(MyMessageBuilder.helpMessage(chatId));
                        }
                        case "/ismni_yangilash" -> {
                            user.setState(UserState.CHANGE_NAME);
                            userRepository.save(user);
                            sender.execute(MyMessageBuilder.changeNameMessage(chatId, user));
                        }
                        case "/dasturchi" -> {
                            sender.execute(MyMessageBuilder.developerUsernameMessage(chatId));
                        }
                    }
                } else {
                    switch (user.getState()) {
                        case MAIN -> {
                            handleMainState(text, user, chatId);
                        }
                        case ADD_TEST -> {
                            handleAddTestState(text, chatId, user);
                        }
                        case CHECK_ANSWER -> {
                            handleCheckAnswerState(text, chatId, user);
                        }
                        case CHANGE_NAME -> {
                            handleChangeName(user, text, chatId);
                        }
                    }
                }
            }
        }
    }

    private void handleChangeName(User user, String text, String chatId) throws TelegramApiException {
        sender.execute(MyMessageBuilder.fullNameChangeCompleteMessage(chatId, text, user));
        user.setFullName(text);
        user.setState(UserState.MAIN);
        userRepository.save(user);
    }

    private void checkFullName(String text, String chatId) throws TelegramApiException {
        Pattern pattern = Pattern.compile("^fi-", Pattern.CASE_INSENSITIVE);
        if (text.equals("/start")) {
            sender.execute(MyMessageBuilder.startBotMessage(chatId));
        } else if (pattern.matcher(text).find()) {
            if (!subscribeToChannel(chatId)) {
                sender.execute(MyMessageBuilder.registrationBotMessage(chatId, false));
            } else {
                sender.execute(MyMessageBuilder.handleSubscribedActionMessage(chatId));
            }
            String name = text.substring(3);
            userRepository.save(new User(chatId, name, UserState.MAIN, Collections.emptyList(), Collections.emptyList()));

        } else {
            sender.execute(MyMessageBuilder.checkFullNameMessage(chatId));
        }
    }

    private void handleMainState(String text, User user, String chatId) throws TelegramApiException {
        switch (text) {
            case "➕ Test yaratish" -> {
                user.setState(UserState.ADD_TEST);
                userRepository.save(user);
                sender.execute(MyMessageBuilder.createTestMessage(chatId));
            }
            case "✅ Javoblarni tekshirish" -> {
                user.setState(UserState.CHECK_ANSWER);
                userRepository.save(user);
                sender.execute(MyMessageBuilder.checkAnswerMessage(chatId));
            }
            case "\uD83D\uDD19 Orqaga qaytish" -> {
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            }
            default -> {
                sender.execute(MyMessageBuilder.wrongCommandMessage(chatId));
            }
        }
    }

    private void handleAddTestState(String text, String chatId, User user) throws TelegramApiException {
        if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            user.setState(UserState.MAIN);
            userRepository.save(user);
            sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
        } else {
            int indexOfLowLine = text.indexOf("_", 6);
            if (!(text.startsWith("+test_") && indexOfLowLine != -1)) {
                sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
            } else if (text.substring(6, indexOfLowLine).isBlank() || text.substring(indexOfLowLine + 1).isBlank()) {
                sender.execute(MyMessageBuilder.secondCreateTestErrorMessage(chatId));
            } else {
                String subjectName = text.substring(6, indexOfLowLine);
                String inputKeys = text.substring(indexOfLowLine + 1);
                String keys = removeNonLettersWithoutReplaceAll(inputKeys);

                Test test = new Test(null, subjectName, keys, true, user, Collections.emptyList());
                testRepository.saveAndFlush(test);

                List<Test> testList = new ArrayList<>();
                testList.add(test);

                user.setTests(testList);
                user.setState(UserState.MAIN);

                testRepository.save(test);
                userRepository.save(user);

                sender.execute(MyMessageBuilder.generatedTestMessage(chatId, test));
            }
        }
    }

    private void handleCheckAnswerState(String text, String chatId, User user) throws TelegramApiException {
        if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            user.setState(UserState.MAIN);
            userRepository.save(user);
            sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
        } else {
            int indexOfMainCharacter = text.indexOf("#");
            String regex = "[a-zA-Z]+";
            if (indexOfMainCharacter == -1) {
                sender.execute(MyMessageBuilder.checkAnswerErrorMessage(chatId));
            } else if (text.substring(0, indexOfMainCharacter).matches(regex)) {
                sender.execute(MyMessageBuilder.checkAnswerErrorMessage(chatId));
            } else {
                String testCode = text.substring(0, indexOfMainCharacter);
                Test test = testRepository.findById(Integer.valueOf(testCode)).orElse(null);
                if (test == null) {
                    sender.execute(MyMessageBuilder.testCodeErrorMessage(chatId));
                } else {
                    if (test.isStatus()) {
                        String correctKeys = test.getKeys();
                        String inputAnswerKeys = text.substring(indexOfMainCharacter + 1);
                        String includedKeys = removeNonLettersWithoutReplaceAll(inputAnswerKeys);

                        if (correctKeys.length() != includedKeys.length()) {
                            sender.execute(MyMessageBuilder.checkAnswerErrorMessage(chatId, test));
                        } else {
                            int count = 0;
                            ArrayList<Integer> wrongAnswers = new ArrayList<>();

                            for (int i = 0; i < correctKeys.length(); i++) {
                                if (correctKeys.charAt(i) == includedKeys.charAt(i)) {
                                    count++;
                                } else {
                                    wrongAnswers.add(i);
                                }
                            }

                            int numberOfKeys = correctKeys.length();
                            int correctAnswer = count * 100 / numberOfKeys;

                            TestSubmission submissionByUserId = testSubmissionRepository.findByUserIdAndTestId(user.getId(), test.getId());
                            if (submissionByUserId == null) {
                                TestSubmission testSubmission = new TestSubmission(null, test, user, count, numberOfKeys, wrongAnswers.toString(), LocalDateTime.now());

                                testSubmissionRepository.save(testSubmission);

                                test.getSubmissions().add(testSubmission);
                                user.getSubmissions().add(testSubmission);
                                String ownerId = test.getUser().getId();
                                user.setState(UserState.MAIN);

                                testRepository.save(test);
                                userRepository.save(user);

                                sender.execute(MyMessageBuilder.testOwnerResultMessage(ownerId, test, user.getFullName()));
                                sender.execute(MyMessageBuilder.testResultMessage(chatId, correctAnswer, test, count, user.getFullName()));
                            } else {
                                sender.execute(MyMessageBuilder.canBeCheckedOnceMessage(chatId));
                            }
                        }
                    } else {
                        sender.execute(MyMessageBuilder.testCompletedMessage(chatId));
                    }
                }
            }
        }
    }

    private void handleCallbackQuery(Update update, String ownerId) throws TelegramApiException {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        if (data.equals("unsubscribed")) {
            boolean subscribeToChannel = subscribeToChannel(ownerId);
            if (subscribeToChannel) {
                sender.execute(MyMessageBuilder.handleSubscribedActionMessage(ownerId));
            } else {
                sender.execute(MyMessageBuilder.handleSubscribeActionMessage(ownerId, false));
            }
        } else if (data.startsWith("currentSituation")) {
            String testId = update.getCallbackQuery().getData().substring("currentSituation".length());
            Test test = testRepository.findById(Integer.valueOf(testId)).get();
            List<TestSubmission> submissions = sortSubmissionsByTestsSolvedDescending(testId);
            sender.execute(MyMessageBuilder.currentStatusOfTestMessage(ownerId, submissions, test));
        } else if (data.startsWith("completeTest")) {
            String testId = update.getCallbackQuery().getData().substring("completeTest".length());
            Test test = testRepository.findById(Integer.valueOf(testId)).get();
            List<TestSubmission> submissions = sortSubmissionsByTestsSolvedDescending(testId);
            test.setStatus(false);
            testRepository.save(test);

            for (TestSubmission submission : submissions) {
                String userId = submission.getUser().getId();
                sender.execute(MyMessageBuilder.testFinishedMessageForUsers(userId, submission, test));
            }

            String keys = formatString(test.getKeys());
            String result = participantResults(submissions);

            sender.execute(MyMessageBuilder.testFinishedMessageForTestOwner(ownerId, test, keys, result));

        }
    }

    private String participantResults(List<TestSubmission> submissions) {
        String[] medals = {"🥇", "🥈", "🥉"};

        StringBuilder results = new StringBuilder();
        int previousScore = -1;
        int medalIndex = 0;
        for (int i = 0; i < submissions.size(); i++) {
            TestSubmission submission = submissions.get(i);
            int currentScore = submission.getCorrectAnswersCount();

            if (currentScore != previousScore && medalIndex < medals.length) {
                previousScore = currentScore;
                medalIndex++;
            }

            String medal = medals[medalIndex - 1];
            results.append(i + 1).append(". ")
                    .append(submission.getUser().getFullName())
                    .append(" - ")
                    .append(currentScore)
                    .append(" ")
                    .append(medal)
                    .append("\n");
        }

        return results.toString();
    }


    private String formatString(String input) {
        StringBuilder formattedString = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            formattedString.append(i + 1).append(".").append(currentChar).append(" ");
        }
        if (!formattedString.isEmpty()) {
            formattedString.setLength(formattedString.length() - 1);
        }
        return formattedString.toString();
    }

    private List<TestSubmission> sortSubmissionsByTestsSolvedDescending(String testId) {
        return testRepository.findById(Integer.valueOf(testId)).get()
                .getSubmissions()
                .stream()
                .sorted(Comparator.comparingInt(TestSubmission::getCorrectAnswersCount).reversed())
                .collect(Collectors.toList());
    }

    private String removeNonLettersWithoutReplaceAll(String keys) {
        StringBuilder filteredString = new StringBuilder();
        for (int i = 0; i < keys.length(); i++) {
            char currentChar = keys.charAt(i);
            if (Character.isLetter(currentChar)) {
                filteredString.append(currentChar);
            }
        }
        return filteredString.toString();
    }

    @SneakyThrows
    private boolean subscribeToChannel(String userId) {
        String firsChannelId = "@pmtestbaza";
        String secondChannelId = "@online_mathtest";

        GetChatMember getFirstChatMember = new GetChatMember(firsChannelId, Long.valueOf(userId));
        GetChatMember getSecondChatMember = new GetChatMember(secondChannelId, Long.valueOf(userId));

        ChatMember firstChatMember = sender.execute(getFirstChatMember);
        ChatMember secondChatMember = sender.execute(getSecondChatMember);

        String firstStatus = firstChatMember.getStatus();
        String secondStatus = secondChatMember.getStatus();

        boolean firstMemberOfChannel = firstStatus.equals("member") || firstStatus.equals("administrator") || firstStatus.equals("creator");
        boolean secondMemberOfChannel = secondStatus.equals("member") || secondStatus.equals("administrator") || secondStatus.equals("creator");

        return firstMemberOfChannel && secondMemberOfChannel;
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
