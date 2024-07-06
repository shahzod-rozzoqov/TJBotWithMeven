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
import uz.shoh.tjbot.entitys.test.BlocTestRepository;
import uz.shoh.tjbot.entitys.test.TestRepository;
import uz.shoh.tjbot.entitys.test.entity.BlocTest;
import uz.shoh.tjbot.entitys.test.entity.Test;
import uz.shoh.tjbot.entitys.testSubmission.BlockTestSubmissionRepository;
import uz.shoh.tjbot.entitys.testSubmission.TestSubmissionRepository;
import uz.shoh.tjbot.entitys.testSubmission.entity.BlocTestSubmission;
import uz.shoh.tjbot.entitys.testSubmission.entity.TestSubmission;
import uz.shoh.tjbot.entitys.user.UserRepository;
import uz.shoh.tjbot.entitys.user.entiry.User;
import uz.shoh.tjbot.entitys.user.enums.UserState;
import uz.shoh.tjbot.message.MyMessageBuilder;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class UpdateHandler {
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final TestSubmissionRepository testSubmissionRepository;
    private final BlocTestRepository blocTestRepository;
    private final BlockTestSubmissionRepository blockTestSubmissionRepository;
    private final DefaultAbsSender sender;
    private final HashMap<String, ArrayList<Test>> map = new HashMap<>();

    private static float calculateTotalScore(BlocTestSubmission submission) {
        float compulsoryScore = 1.1f * (submission.getFirstCompulsorySubjectCorrectAnswersCount() +
                submission.getSecondCompulsorySubjectCorrectAnswersCount() +
                submission.getThirdCompulsorySubjectCorrectAnswersCount());
        float majorScore = 3.1f * submission.getFirstMajorSubjectCorrectAnswersCount() +
                2.1f * submission.getSecondMajorSubjectCorrectAnswersCount();
        return compulsoryScore + majorScore;
    }

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
            checkFullName(text, chatId, update);
        } else {
            boolean subscribeToChannel = subscribeToChannel(user.getId());
            if (!subscribeToChannel) {
                sender.execute(MyMessageBuilder.handleSubscribeActionMessage(user.getId(), false));
            } else {
                if (text.startsWith("/")) {
                    switch (text) {
                        case "/start" -> {
                            user.setState(UserState.MAIN);
                            userRepository.save(user);

                            deleteIncompleteObjects(user);
                            sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
                        }
                        case "/yordam" -> {
                            deleteIncompleteObjects(user);
                            sender.execute(MyMessageBuilder.helpMessage(chatId));
                        }
                        case "/ismni_yangilash" -> {
                            user.setState(UserState.CHANGE_NAME);
                            userRepository.save(user);
                            deleteIncompleteObjects(user);
                            sender.execute(MyMessageBuilder.changeNameMessage(chatId, user));
                        }
                        case "/dasturchi" -> {
                            sender.execute(MyMessageBuilder.developerUsernameMessage(chatId));
                        }
                    }
                } else {
                    switch (user.getState()) {
                        case MAIN -> handleMainState(text, user, chatId);
                        case ADD_TEST_SUBJECT -> handleAddTestSubject(text, chatId, user);
                        case ADD_TEST_KEYS -> handleAddTestKeys(text, chatId, user);
                        case ADD_BLOC_TEST_FIRST_COMPULSORY_KEYS ->
                                handleAddBlocTestFirstCompulsoryKeys(text, chatId, user);
                        case ADD_BLOC_TEST_SECOND_COMPULSORY_KEYS ->
                                handleAddBlocTestSecondCompulsoryKeys(text, chatId, user);
                        case ADD_BLOC_TEST_THIRD_COMPULSORY_KEYS ->
                                handleAddBlocTestThirdCompulsoryKeys(text, chatId, user);
                        case ADD_BLOC_TEST_FIRST_MAJOR_SUBJECT ->
                                handleAddBlocTestFirstMajorSubjectName(text, chatId, user);
                        case ADD_BLOC_TEST_FIRST_MAJOR_SUBJECT_KEYS ->
                                handleAddBlocTestFirstMajorSubjectKeys(text, chatId, user);
                        case ADD_BLOC_TEST_SECOND_MAJOR_SUBJECT ->
                                handleAddBlocTestSecondMajorSubjectName(text, chatId, user);
                        case ADD_BLOC_TEST_SECOND_MAJOR_SUBJECT_KEYS ->
                                handleAddBlocTestSecondMajorSubjectKeys(text, chatId, user);
                        case CHECK_TEST_ANSWER -> handleCheckTestAnswerState(text, chatId, user);
                        case CHECK_BLOCK_TEST_FIRST_COMPULSORY_SUBJECT_ANSWER, CHECK_BLOCK_TEST_SECOND_COMPULSORY_SUBJECT_ANSWER, CHECK_BLOCK_TEST_THIRD_COMPULSORY_SUBJECT_ANSWER, CHECK_BLOCK_TEST_FIRST_MAJOR_SUBJECT_ANSWER, CHECK_BLOCK_TEST_SECOND_MAJOR_SUBJECT_ANSWER ->
                                handleBlockCheckAnswerState(text, chatId, user);
                        case CHANGE_NAME -> handleChangeName(user, text, chatId);
                    }
                }
            }
        }
    }

    private void deleteIncompleteObjects(User user) {
        List<Test> tests = user.getTests();
        List<BlocTest> blocTests = user.getBlocTests();
        if (tests != null || blocTests != null) {
            assert tests != null;
            for (Test myTest : tests) {
                if (myTest.getKeys().isBlank()) {
                    testRepository.delete(myTest);
                }
                for (TestSubmission submission : myTest.getSubmissions()) {
                    if (submission.getCorrectAnswersCount() == 0) {
                        testSubmissionRepository.delete(submission);
                    }
                }
            }
            for (BlocTest myBlockTest : blocTests) {
                for (Test test : myBlockTest.getTests()) {
                    if (test.getKeys().isBlank() || test.getBlocTest() == null) {
                        testRepository.delete(test);
                    }
                    for (BlocTestSubmission blockTestSubmission : myBlockTest.getBlocTestSubmissions()) {
                        if (blockTestSubmission.getSecondMajorSubjectTotalAnswersCount() == 0) {
                            blockTestSubmissionRepository.delete(blockTestSubmission);
                        }
                    }
                }
            }
        }
    }

    private void checkFullName(String text, String chatId, Update update) throws TelegramApiException {

        if (text.equals("/start") || text.equals("➕ Test yaratish") || text.equals("📗Oddiy test") || text.equals("📚Blok test") ||
                text.equals("✅ Javoblarni tekshirish") || text.equals("✅Oddiy testni tekshirish") ||
                text.equals("☑️Blok testni tekshirish") || text.equals("\uD83D\uDD19 Orqaga qaytish") ||
                text.equals("/ismni_yangilash") || text.equals("/dasturchi") || text.equals("/yordam")) {
            sender.execute(MyMessageBuilder.startBotMessage(chatId));
        } else {
            if (!subscribeToChannel(chatId)) {
                sender.execute(MyMessageBuilder.registrationBotMessage(chatId, false));
            } else {
                sender.execute(MyMessageBuilder.handleSubscribedActionMessage(chatId, text));
            }
            userRepository.save(new User(chatId, text, UserState.MAIN, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        }
    }

    private void handleMainState(String text, User user, String chatId) throws TelegramApiException {
        switch (text) {
            case "➕ Test yaratish" -> {
                sender.execute(MyMessageBuilder.createTestMessage(chatId));
            }
            case "📗Oddiy test" -> {
                user.setState(UserState.ADD_TEST_SUBJECT);
                userRepository.save(user);
                sender.execute(MyMessageBuilder.subjectNameInputMessage(chatId));
            }
            case "📚Blok test" -> {
                user.setState(UserState.ADD_BLOC_TEST_FIRST_COMPULSORY_KEYS);
                userRepository.save(user);
                sender.execute(MyMessageBuilder.enterFirstCompulsorySubjectKeyInputMessage(chatId));
            }
            case "✅ Javoblarni tekshirish" -> {
                sender.execute(MyMessageBuilder.checkTestMessage(chatId));
            }
            case "✅Oddiy testni tekshirish" -> {
                user.setState(UserState.CHECK_TEST_ANSWER);
                userRepository.save(user);
                sender.execute(MyMessageBuilder.checkAnswerMessage(chatId));
            }
            case "☑️Blok testni tekshirish" -> {
                user.setState(UserState.CHECK_BLOCK_TEST_FIRST_COMPULSORY_SUBJECT_ANSWER);
                userRepository.save(user);
                sender.execute(MyMessageBuilder.checkFirstCompulsoryBlockTestAnswerMessage(chatId));
            }

            case "\uD83D\uDD19 Orqaga qaytish" -> {
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            }
            default -> {
                sender.execute(MyMessageBuilder.wrongCommandMessage(chatId));
            }
        }
    }

    private void handleAddTestSubject(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                if (text.isBlank()) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    Test test = new Test(null, text, null, false, user, null, Collections.emptyList());
                    testRepository.saveAndFlush(test);
                    user.setState(UserState.ADD_TEST_KEYS);
                    userRepository.save(user);
                    sender.execute(MyMessageBuilder.enterCorrectAnswerMessage(test.getId(), chatId));
                }
            }
        }
    }

    public void handleAddTestKeys(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            int indexOfMainCharacter = text.indexOf("#");
            String regex = "[a-zA-Z]+";
            if (indexOfMainCharacter == -1 || text.substring(0, indexOfMainCharacter).matches(regex)) {
                sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
            } else {
                String id = text.substring(0, indexOfMainCharacter);
                Test test = testRepository.findByIdAndUserId(Integer.valueOf(id), chatId);
                if (test == null || test.isStatus()) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    String keys = removeNonLettersWithoutReplaceAll(text);
                    test.setStatus(true);
                    test.setKeys(keys);

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
    }

    public void handleAddBlocTestFirstCompulsoryKeys(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                if (text.isBlank()) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    String keys = removeNonLettersWithoutReplaceAll(text);
                    Test test = new Test(null, "Ona tili", keys, true, user, null, Collections.emptyList());

                    ArrayList<Test> tests = new ArrayList<>();
                    tests.add(test);
                    map.put(chatId, tests);

                    testRepository.save(test);
                    user.setState(UserState.ADD_BLOC_TEST_SECOND_COMPULSORY_KEYS);

                    userRepository.save(user);

                    sender.execute(MyMessageBuilder.enterSecondCompulsorySubjectKeyInputMessage(chatId));
                }
            }
        }
    }

    public void handleAddBlocTestSecondCompulsoryKeys(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                if (text.isBlank()) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    String keys = removeNonLettersWithoutReplaceAll(text);
                    Test test = new Test(null, "Matematika", keys, true, user, null, Collections.emptyList());
                    ArrayList<Test> tests = map.get(chatId);
                    tests.add(test);
                    map.put(chatId, tests);

                    testRepository.save(test);
                    user.setState(UserState.ADD_BLOC_TEST_THIRD_COMPULSORY_KEYS);

                    userRepository.save(user);
                    sender.execute(MyMessageBuilder.enterThirdCompulsorySubjectKeyInputMessage(chatId));
                }
            }
        }
    }

    public void handleAddBlocTestThirdCompulsoryKeys(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                if (text.isBlank()) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    String keys = removeNonLettersWithoutReplaceAll(text);
                    Test test = new Test(null, "Tarix", keys, true, user, null, Collections.emptyList());
                    ArrayList<Test> tests = map.get(chatId);
                    tests.add(test);
                    map.put(chatId, tests);

                    testRepository.save(test);
                    user.setState(UserState.ADD_BLOC_TEST_FIRST_MAJOR_SUBJECT);

                    userRepository.save(user);
                    sender.execute(MyMessageBuilder.enterFirstMajorSubjectNameInputMessage(chatId));
                }
            }
        }
    }

    public void handleAddBlocTestFirstMajorSubjectName(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                if (text.isBlank()) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    Test test = new Test(null, text, null, false, user, null, Collections.emptyList());

                    testRepository.save(test);
                    user.setState(UserState.ADD_BLOC_TEST_FIRST_MAJOR_SUBJECT_KEYS);

                    userRepository.save(user);
                    sender.execute(MyMessageBuilder.enterFirstMajorSubjectKeysInputMessage(test.getId(), chatId));
                }
            }
        }
    }

    public void handleAddBlocTestFirstMajorSubjectKeys(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                int indexOfMainCharacter = text.indexOf("#");
                String regex = "[a-zA-Z]+";
                if (indexOfMainCharacter == -1 || text.substring(0, indexOfMainCharacter).matches(regex)) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    String id = text.substring(0, indexOfMainCharacter);
                    Test test = testRepository.findByIdAndUserId(Integer.valueOf(id), chatId);
                    if (test == null || test.isStatus()) {
                        sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                    } else {
                        String keys = removeNonLettersWithoutReplaceAll(text);
                        test.setKeys(keys);
                        test.setStatus(true);
                        testRepository.save(test);

                        ArrayList<Test> tests = map.get(chatId);
                        tests.add(test);
                        map.put(chatId, tests);

                        user.setState(UserState.ADD_BLOC_TEST_SECOND_MAJOR_SUBJECT);
                        userRepository.save(user);
                        sender.execute(MyMessageBuilder.enterSecondMajorSubjectNameInputMessage(chatId));
                    }
                }
            }
        }
    }

    public void handleAddBlocTestSecondMajorSubjectName(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                if (text.isBlank()) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    Test test = new Test(null, text, null, false, user, null, Collections.emptyList());

                    testRepository.save(test);
                    user.setState(UserState.ADD_BLOC_TEST_SECOND_MAJOR_SUBJECT_KEYS);

                    userRepository.save(user);
                    sender.execute(MyMessageBuilder.enterSecondMajorSubjectKeysInputMessage(test.getId(), chatId));
                }
            }
        }
    }

    public void handleAddBlocTestSecondMajorSubjectKeys(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                int indexOfMainCharacter = text.indexOf("#");
                String regex = "[a-zA-Z]+";

                if (indexOfMainCharacter == -1 || text.substring(0, indexOfMainCharacter).matches(regex)) {
                    sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                } else {
                    String id = text.substring(0, indexOfMainCharacter);
                    Test test = testRepository.findByIdAndUserId(Integer.valueOf(id), chatId);
                    if (test == null || test.isStatus()) {
                        sender.execute(MyMessageBuilder.createTestErrorMessage(chatId));
                    } else {
                        String keys = removeNonLettersWithoutReplaceAll(text);
                        test.setKeys(keys);
                        test.setStatus(true);
                        test.setUser(user);

                        ArrayList<Test> tests = map.get(chatId);
                        tests.add(test);

                        BlocTest blockTest = new BlocTest(null, true, user, null, Collections.emptyList());
                        for (Test myTest : tests) {
                            myTest.setBlocTest(blockTest);
                        }

                        blockTest.setTests(tests);
                        blocTestRepository.save(blockTest);

                        ArrayList<BlocTest> blockTests = new ArrayList<>();
                        blockTests.add(blockTest);

                        user.setBlocTests(blockTests);
                        user.setTests(tests);
                        user.setState(UserState.MAIN);
                        userRepository.save(user);
                        testRepository.save(test);
                        sender.execute(MyMessageBuilder.addedTestDatabaseMessage(chatId, blockTest));
                    }
                }
            }
        }
    }

    private void handleCheckTestAnswerState(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                int indexOfMainCharacter = text.indexOf("#");
                String regex = "[a-zA-Z]+";
                if (indexOfMainCharacter == -1 || text.substring(0, indexOfMainCharacter).matches(regex)) {
                    sender.execute(MyMessageBuilder.checkTestAnswerErrorMessage(chatId));
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
                                sender.execute(MyMessageBuilder.testAnswersAreIncomplete(chatId, test));
                            } else {
                                int count = 0;
                                ArrayList<Integer> wrongAnswers = new ArrayList<>();

                                for (int i = 0; i < correctKeys.length(); i++) {
                                    if (correctKeys.charAt(i) == includedKeys.charAt(i)) {
                                        count++;
                                    } else {
                                        wrongAnswers.add(i + 1);
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

    private void handleBlockCheckAnswerState(String text, String chatId, User user) throws TelegramApiException {
        if (text.length() > 200) {
            sender.execute(MyMessageBuilder.wrongTextEnteredMessage(chatId));
        } else {
            if (text.equals("\uD83D\uDD19 Orqaga qaytish") || text.equals("/start")) {
                user.setState(UserState.MAIN);
                userRepository.save(user);
                deleteIncompleteObjects(user);
                sender.execute(MyMessageBuilder.goToMainMenuMessage(chatId));
            } else {
                int indexOfMainCharacter = text.indexOf("#");
                String regex = "[a-zA-Z]+";
                if (indexOfMainCharacter == -1 || text.substring(0, indexOfMainCharacter).matches(regex)) {
                    sender.execute(MyMessageBuilder.checkTestAnswerErrorMessage(chatId));
                } else {
                    String testCode = text.substring(0, indexOfMainCharacter);
                    BlocTest blockTest = blocTestRepository.findById(Integer.valueOf(testCode)).orElse(null);
                    if (blockTest == null) {
                        sender.execute(MyMessageBuilder.testCodeErrorMessage(chatId));
                    } else {
                        if (blockTest.isStatus()) {

                            List<Test> tests = blockTest.getTests();
                            Comparator<Test> comparator = Comparator.comparingInt(Test::getId);
                            tests.sort(comparator);

                            Test test = findTestInBlocktest(user, tests);
                            String correctKeys = test.getKeys();
                            String inputAnswerKeys = text.substring(indexOfMainCharacter + 1);
                            String includedKeys = removeNonLettersWithoutReplaceAll(inputAnswerKeys);

                            if (correctKeys.length() != includedKeys.length()) {
                                sender.execute(MyMessageBuilder.blockTestAnswersAreIncomplete(chatId, blockTest, test));
                            } else {
                                int correctAnswersCount = 0;
                                ArrayList<Integer> wrongAnswers = new ArrayList<>();

                                for (int j = 0; j < correctKeys.length(); j++) {
                                    if (correctKeys.charAt(j) == includedKeys.charAt(j)) {
                                        correctAnswersCount++;
                                    } else {
                                        wrongAnswers.add(j + 1);
                                    }
                                }

                                int totalAnswerCount = correctKeys.length();
                                BlocTestSubmission blocTestSubmissionById = blockTestSubmissionRepository.findByUserIdAndBlocTestId(user.getId(), blockTest.getId());

                                if (blocTestSubmissionById != null && blocTestSubmissionById.isAnswerIsCollected()) {
                                    sender.execute(MyMessageBuilder.canBeCheckedOnceMessage(chatId));
                                } else {
                                    switch (user.getState()) {
                                        case CHECK_BLOCK_TEST_FIRST_COMPULSORY_SUBJECT_ANSWER -> {
                                            BlocTestSubmission blocTestSubmission = new BlocTestSubmission(null, false, blockTest, user, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null, null, null, null, LocalDateTime.now());
                                            blocTestSubmission.setFirstCompulsorySubjectCorrectAnswersCount(correctAnswersCount);
                                            blocTestSubmission.setFirstCompulsorySubjectTotalAnswersCount(totalAnswerCount);
                                            blocTestSubmission.setFirstCompulsorySubjectWrongAnswers(wrongAnswers.toString());
                                            blockTestSubmissionRepository.save(blocTestSubmission);

                                            user.setState(UserState.CHECK_BLOCK_TEST_SECOND_COMPULSORY_SUBJECT_ANSWER);
                                            userRepository.save(user);
                                            sender.execute(MyMessageBuilder.checkSecondCompulsoryBlockTestAnswerMessage(chatId));
                                        }
                                        case CHECK_BLOCK_TEST_SECOND_COMPULSORY_SUBJECT_ANSWER -> {
                                            BlocTestSubmission blocTestSubmission = blockTestSubmissionRepository.findByUserIdAndBlocTestId(user.getId(), blockTest.getId());
                                            if (Integer.parseInt(testCode) != (blockTest.getId()) || blocTestSubmission == null) {
                                                sender.execute(MyMessageBuilder.blockTestCodeIsIncorrect(chatId, blockTest.getId()));
                                            } else {
                                                blocTestSubmission.setSecondCompulsorySubjectCorrectAnswersCount(correctAnswersCount);
                                                blocTestSubmission.setSecondCompulsorySubjectTotalAnswersCount(totalAnswerCount);
                                                blocTestSubmission.setSecondCompulsorySubjectWrongAnswers(wrongAnswers.toString());

                                                user.setState(UserState.CHECK_BLOCK_TEST_THIRD_COMPULSORY_SUBJECT_ANSWER);
                                                userRepository.save(user);
                                                sender.execute(MyMessageBuilder.checkThirdCompulsoryBlockTestAnswerMessage(chatId));
                                            }
                                        }
                                        case CHECK_BLOCK_TEST_THIRD_COMPULSORY_SUBJECT_ANSWER -> {
                                            BlocTestSubmission blocTestSubmission = blockTestSubmissionRepository.findByUserIdAndBlocTestId(user.getId(), blockTest.getId());
                                            if (Integer.parseInt(testCode) != (blockTest.getId()) || blocTestSubmission == null) {
                                                sender.execute(MyMessageBuilder.blockTestCodeIsIncorrect(chatId, blockTest.getId()));
                                            } else {
                                                blocTestSubmission.setThirdCompulsorySubjectCorrectAnswersCount(correctAnswersCount);
                                                blocTestSubmission.setThirdCompulsorySubjectTotalAnswersCount(totalAnswerCount);
                                                blocTestSubmission.setThirdCompulsorySubjectWrongAnswers(wrongAnswers.toString());

                                                user.setState(UserState.CHECK_BLOCK_TEST_FIRST_MAJOR_SUBJECT_ANSWER);
                                                userRepository.save(user);
                                                sender.execute(MyMessageBuilder.checkFirstMajorBlockTestAnswerMessage(chatId));
                                            }
                                        }
                                        case CHECK_BLOCK_TEST_FIRST_MAJOR_SUBJECT_ANSWER -> {
                                            BlocTestSubmission blocTestSubmission = blockTestSubmissionRepository.findByUserIdAndBlocTestId(user.getId(), blockTest.getId());
                                            if (Integer.parseInt(testCode) != (blockTest.getId()) || blocTestSubmission == null) {
                                                sender.execute(MyMessageBuilder.blockTestCodeIsIncorrect(chatId, blockTest.getId()));
                                            } else {
                                                blocTestSubmission.setFirstMajorSubjectCorrectAnswersCount(correctAnswersCount);
                                                blocTestSubmission.setFirstMajorSubjectTotalAnswersCount(totalAnswerCount);
                                                blocTestSubmission.setFirstMajorSubjectWrongAnswers(wrongAnswers.toString());

                                                user.setState(UserState.CHECK_BLOCK_TEST_SECOND_MAJOR_SUBJECT_ANSWER);
                                                userRepository.save(user);
                                                sender.execute(MyMessageBuilder.checkSecondMajorBlockTestAnswerMessage(chatId));
                                            }
                                        }
                                        case CHECK_BLOCK_TEST_SECOND_MAJOR_SUBJECT_ANSWER -> {
                                            BlocTestSubmission blocTestSubmission = blockTestSubmissionRepository.findByUserIdAndBlocTestId(user.getId(), blockTest.getId());

                                            if (Integer.parseInt(testCode) != (blockTest.getId()) || blocTestSubmission == null) {
                                                sender.execute(MyMessageBuilder.blockTestCodeIsIncorrect(chatId, blockTest.getId()));
                                            } else {
                                                blocTestSubmission.setSecondMajorSubjectCorrectAnswersCount(correctAnswersCount);
                                                blocTestSubmission.setSecondMajorSubjectTotalAnswersCount(totalAnswerCount);
                                                blocTestSubmission.setSecondMajorSubjectWrongAnswers(wrongAnswers.toString());
                                                blockTest.getBlocTestSubmissions().add(blocTestSubmission);
                                                blockTest.getBlocTestSubmissions().add(blocTestSubmission);
                                                blocTestSubmission.setAnswerIsCollected(true);
                                                blockTestSubmissionRepository.save(blocTestSubmission);

                                                user.setState(UserState.MAIN);
                                                userRepository.save(user);
                                                String ownerId = blockTest.getUser().getId();

                                                testRepository.save(test);
                                                float totalScore = calculateTotalScore(blocTestSubmission);
                                                int numberOfCorrectAnswer = blocTestSubmission.getFirstCompulsorySubjectCorrectAnswersCount() +
                                                        blocTestSubmission.getSecondCompulsorySubjectCorrectAnswersCount() +
                                                        blocTestSubmission.getThirdCompulsorySubjectCorrectAnswersCount() +
                                                        blocTestSubmission.getFirstMajorSubjectCorrectAnswersCount() +
                                                        blocTestSubmission.getSecondMajorSubjectCorrectAnswersCount();
                                                int totalNumberOfQuestions = blocTestSubmission.getFirstCompulsorySubjectTotalAnswersCount() +
                                                        blocTestSubmission.getSecondCompulsorySubjectTotalAnswersCount() +
                                                        blocTestSubmission.getThirdCompulsorySubjectTotalAnswersCount() +
                                                        blocTestSubmission.getFirstMajorSubjectTotalAnswersCount() +
                                                        blocTestSubmission.getSecondMajorSubjectTotalAnswersCount();

                                                sender.execute(MyMessageBuilder.blockTestOwnerResultMessage(ownerId, blockTest, user.getFullName()));
                                                sender.execute(MyMessageBuilder.blockTestResultMessage(chatId, blockTest, tests, blocTestSubmission, totalScore, totalNumberOfQuestions, numberOfCorrectAnswer, user.getFullName()));
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            sender.execute(MyMessageBuilder.testCompletedMessage(chatId));
                        }
                    }
                }
            }
        }
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

    private Test findTestInBlocktest(User user, List<Test> tests) {
        switch (user.getState()) {
            case CHECK_BLOCK_TEST_FIRST_COMPULSORY_SUBJECT_ANSWER -> {
                return tests.get(0);
            }
            case CHECK_BLOCK_TEST_SECOND_COMPULSORY_SUBJECT_ANSWER -> {
                return tests.get(1);
            }
            case CHECK_BLOCK_TEST_THIRD_COMPULSORY_SUBJECT_ANSWER -> {
                return tests.get(2);
            }
            case CHECK_BLOCK_TEST_FIRST_MAJOR_SUBJECT_ANSWER -> {
                return tests.get(3);
            }
            case CHECK_BLOCK_TEST_SECOND_MAJOR_SUBJECT_ANSWER -> {
                return tests.get(4);
            }
        }
        return null;
    }

    private String removeNonLettersWithoutReplaceAll(String keys) {
        StringBuilder filteredString = new StringBuilder();
        for (int i = 0; i < keys.length(); i++) {
            char currentChar = keys.charAt(i);
            if (Character.isLetter(currentChar)) {
                filteredString.append(currentChar);
            }
        }
        return filteredString.toString().toLowerCase();
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
        } else if (data.startsWith("currentStatusForTest")) {
            String testId = update.getCallbackQuery().getData().substring("currentStatusForTest".length());
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

        } else if (data.startsWith("currentStatusForBlockTest")) {
            String testId = update.getCallbackQuery().getData().substring("currentStatusForBlockTest".length());

            BlocTest blockTest = blocTestRepository.findById(Integer.valueOf(testId)).get();
            List<BlocTestSubmission> blocTestSubmissions = blockTest.getBlocTestSubmissions();
            List<Test> tests = blockTest.getTests();
            Comparator<Test> comparator = Comparator.comparingInt(Test::getId);
            tests.sort(comparator);

            int totalNumberOfQuestions = blockTest.getTests().get(0).getKeys().length() + blockTest.getTests().get(1).getKeys().length() +
                    blockTest.getTests().get(2).getKeys().length() + blockTest.getTests().get(3).getKeys().length() +
                    blockTest.getTests().get(4).getKeys().length();

            sender.execute(MyMessageBuilder.currentStatusOfBlockTestMessage(ownerId, blocTestSubmissions, tests, blockTest, totalNumberOfQuestions));

        } else if (data.startsWith("completeBlockTest")) {
            String blockTestId = update.getCallbackQuery().getData().substring("completeBlockTest".length());

            BlocTest blockTest = blocTestRepository.findById(Integer.valueOf(blockTestId)).get();
            List<BlocTestSubmission> blocTestSubmissions = blockTest.getBlocTestSubmissions();

            blockTest.setStatus(false);
            blocTestRepository.save(blockTest);
            List<Test> tests = blockTest.getTests();
            Comparator<Test> comparator = Comparator.comparingInt(Test::getId);
            tests.sort(comparator);

            for (BlocTestSubmission blockTestSubmission : blocTestSubmissions) {
                String userId = blockTestSubmission.getUser().getId();
                sender.execute(MyMessageBuilder.blockTestFinishedMessageForUsers(userId, blockTestSubmission, blockTest, tests));
            }

            String firstCompulsorySubjectKeys = formatString(tests.get(0).getKeys());
            String secondCompulsorySubjectKeys = formatString(tests.get(1).getKeys());
            String thirdCompulsorySubjectKeys = formatString(tests.get(2).getKeys());
            String firstMajorSubjectKeys = formatString(tests.get(3).getKeys());
            String secondMajorSubjectKeys = formatString(tests.get(4).getKeys());

            String result = participantResultsForBlockTest(blocTestSubmissions);

            int totalNumberOfQuestions = blockTest.getTests().get(0).getKeys().length() + blockTest.getTests().get(1).getKeys().length() +
                    blockTest.getTests().get(2).getKeys().length() + blockTest.getTests().get(3).getKeys().length() +
                    blockTest.getTests().get(4).getKeys().length();

            sender.execute(MyMessageBuilder.blockTestFinishedMessageForTestOwner(ownerId, blockTest, tests, totalNumberOfQuestions, firstCompulsorySubjectKeys,
                    secondCompulsorySubjectKeys, thirdCompulsorySubjectKeys, firstMajorSubjectKeys, secondMajorSubjectKeys, result));
        }
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    private String participantResultsForBlockTest(List<BlocTestSubmission> blockTestSubmissions) {
        blockTestSubmissions.sort(Comparator.comparing(UpdateHandler::calculateTotalScore).reversed());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < blockTestSubmissions.size(); i++) {
            BlocTestSubmission blockTestSubmission = blockTestSubmissions.get(i);
            float totalScore = calculateTotalScore(blockTestSubmission);

            DecimalFormat decimalFormat = new DecimalFormat("###.#");
            String format = decimalFormat.format(totalScore);
            result.append(i + 1).append(". ")
                    .append(blockTestSubmission.getUser().getFullName()).append(": ")
                    .append(" ( ").append(format).append(" ball) ").append("\n");
        }

        return result.toString();
    }

    @SneakyThrows
    private boolean subscribeToChannel(String userId) {
        String firsChannelId = "@pmtestbaza";
        String secondChannelId = "@mathtest_online";

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

    private void handleChangeName(User user, String text, String chatId) throws TelegramApiException {
        sender.execute(MyMessageBuilder.fullNameChangeCompleteMessage(chatId, text, user));
        user.setFullName(text);
        user.setState(UserState.MAIN);
        userRepository.save(user);
    }
}
