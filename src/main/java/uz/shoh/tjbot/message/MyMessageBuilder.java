package uz.shoh.tjbot.message;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.shoh.tjbot.entitys.test.entity.BlocTest;
import uz.shoh.tjbot.entitys.test.entity.Test;
import uz.shoh.tjbot.entitys.testSubmission.entity.BlocTestSubmission;
import uz.shoh.tjbot.entitys.testSubmission.entity.TestSubmission;
import uz.shoh.tjbot.entitys.user.entiry.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MyMessageBuilder {
    public static SendMessage startBotMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                         ✋ Assalomu alaykum. Botimizga xush kelibsiz!
                         ✏️ Familiya ismingizni kiriting:
                                                  
                         Misol:
                         Eshmatov Toshmat
                         
                         (❗🤖Bot test rejimda ishlayabdi!
                         Takliflar va shikoyatlar bo'lsa @sh_rozzoqov ga yozing🤝)
                         
                        """);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        return sendMessage;
    }

    public static SendMessage registrationBotMessage(String chatId, boolean isSubscribed) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        🤝 Ro'yxatdan o'tganingiz uchun rahmat.
                        Botimizdan to'liq foydalanishingiz uchun
                        @pmtestbaza va @mathtest_online kanallariga obuna bo'lishingiz kerak!
                        """
        );
        sendMessage.setReplyMarkup(createSubscriptionKeyboardMessage());
        return sendMessage;
    }

    public static SendMessage wrongCommandMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ❗ Noto'g'ri buyruq kiritdingiz.
                        👇 Quyidagilardan birini tanlang
                        """
        );
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }

    public static SendMessage handleSubscribeActionMessage(String chatId, boolean isSubscribed) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        🧏‍♂ Kanalimizga obuna bo'lish uchun
                        ➕ Kanalga o'tish tugmasini bosing
                        """
        );
        sendMessage.setReplyMarkup(createSubscriptionKeyboardMessage());
        return sendMessage;
    }

    public static SendMessage handleSubscribedActionMessage(String chatId, String text) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        Ismingiz  %s tarzida saqlandi.
                        💯 Botimizdan to'liq foydalanishingiz mumkin.
                        """.formatted(text)
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }
    public static SendMessage handleSubscribedActionMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        💯 Botimizdan to'liq foydalanishingiz mumkin.
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }
    public static SendMessage createTestMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        Qanaqa turdagi test yaratishni xohlaysiz
                                                
                        (💡 Oddiy test va Blok test haqida
                        /yordam bo'limidan ma'lumot olishingiz mumkin)
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(createTestButton());
        return sendMessage;
    }

    public static SendMessage checkTestMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        Qanaqa turdagi test tekshirishni xohlaysiz
                                                
                        (💡 Oddiy test va Blok test haqida
                        /yordam bo'limidan ma'lumot olishingiz mumkin)
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(checkTestButton());
        return sendMessage;
    }

    public static SendMessage subjectNameInputMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        📝 Test yaratish uchun Fan(yoki test) nomini kiriting.
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage wrongTextEnteredMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ❗Kiritilgan xabar matni 200 belgigan oshmasligi kerak.
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage enterFirstCompulsorySubjectKeyInputMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        📝 Test yaratish uchun 1 - Majburiy fan(Ona tili) kalitlari (to'g'ri javoblari) ni kiriting.
                                                
                        Misol:
                        abccd... yoki 1a2b3c4c5d...
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage enterSecondCompulsorySubjectKeyInputMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        📝 2 - Majburiy fan(Matematika) kalitlari (to'g'ri javoblari) ni kiriting.
                                                
                        Misol:
                        abccd... yoki 1a2b3c4c5d...
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
    }

    public static SendMessage enterThirdCompulsorySubjectKeyInputMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        📝 3 - Majburiy fan(Tarix) kalitlari (to'g'ri javoblari) ni kiriting.
                                                
                        Misol:
                        abccd... yoki 1a2b3c4c5d...
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
    }

    public static SendMessage enterFirstMajorSubjectNameInputMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        📝 Birinchi asosiy Fan nomini kiriting.
                                                
                        Misol:
                        Matematika
                        """
        );
    }

    public static SendMessage enterFirstMajorSubjectKeysInputMessage(Integer testId, String chatId) {
        return new SendMessage(
                chatId,
                """
                        📝 Birinchi asosiy fan kalitlari (to'g'ri javoblari) ni kiriting.
                                                
                        Misol:
                        %s#abccd... yoki %s#1a2b3c4c5d...
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """.formatted(testId, testId)
        );
    }

    public static SendMessage enterSecondMajorSubjectNameInputMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        📝 Ikkinchi asosiy Fan nomini kiriting.
                                                
                        Misol:
                        Fizika
                        """
        );
    }

    public static SendMessage enterSecondMajorSubjectKeysInputMessage(Integer testId, String chatId) {
        return new SendMessage(
                chatId,
                """
                        📝 Ikkinchi asosiy fan kalitlari (to'g'ri javoblari) ni kiriting.
                                                
                        Misol:
                        %s#abccd... yoki %s#1a2b3c4c5d...
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """.formatted(testId, testId)
        );
    }

    public static SendMessage addedTestDatabaseMessage(String chatId, BlocTest blocTest) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ✅ Blok test bazaga qo'shildi
                                                
                        🔢Blok test kodi: %d
                        📗1 - Majburiy fan: %s (%d ta)
                        📗2 - Majburiy fan: %s (%d ta)
                        📗3 - Majburiy fan: %s (%d ta)
                                                
                        📚1 - Asosiy fan: %s (%d ta)
                        📚2 - Asosiy fan: %s (%d ta)
                                                
                        Testda qatnashganlar o'z javoblarini quyidagi ko'rinishida yuborishlari mumkin:
                                                
                        Namuna:
                        *Blok test kodi # Majburiy yoki asosiy fan uchun o'z javoblaringiz*
                                                
                        Misol uchun:
                        *%d#abcdac...
                        yoki
                        %d#1a2b3c4d5a6c...*
                        """.formatted(blocTest.getId(), blocTest.getTests().get(0).getSubjectName(), blocTest.getTests().get(0).getKeys().length(), blocTest.getTests().get(1).getSubjectName(),
                        blocTest.getTests().get(1).getKeys().length(), blocTest.getTests().get(2).getSubjectName(), blocTest.getTests().get(2).getKeys().length(),
                        blocTest.getTests().get(3).getSubjectName(), blocTest.getTests().get(3).getKeys().length(), blocTest.getTests().get(4).getSubjectName(),
                        blocTest.getTests().get(4).getKeys().length(), blocTest.getId(), blocTest.getId())
        );
        sendMessage.setParseMode("markdown");
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }

    public static SendMessage enterCorrectAnswerMessage(Integer testId, String chatId) {
        return new SendMessage(
                chatId,
                """
                        📝Test kalitlari (to'g'ri javoblari) ni kiriting.
                                                
                        Misol:
                        %s#abccd... yoki %s#1a2b3c4c5d...
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """.formatted(testId, testId)
        );
    }

    public static SendMessage createTestErrorMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        ❗Hurmatli foydalanuvchi.
                        Test yaratish uchun to'g'ri ma'lumot kiritishingiz kerak.
                        👆🏻Yuqoridagi xabardaning misoliga qarang.
                        Son va # bir xil bo'lishi kerak!
                        """
        );
    }

    public static SendMessage generatedTestMessage(String chatId, Test test) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ✅ Test bazaga qo'shildi
                                                
                        🔢Test kodi: %d
                        📚Fan nomi: %s
                        ✍️Savollar soni: %d
                                                
                        Testda qatnashganlar o'z javoblarini quyidagi ko'rinishida yuborishlari mumkin:
                                                
                        Namuna:
                        *Test kodi # O'z javoblaringiz*
                                                
                        Misol uchun:
                        *%d#abcdac...
                        yoki
                        %d#1a2b3c4d5a6c...*
                        """.formatted(test.getId(), test.getSubjectName(),
                        test.getKeys().length(), test.getId(), test.getId())
        );
        sendMessage.setParseMode("markdown");
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }

    public static SendMessage goToMainMenuMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        🏠 Asosiy oyna
                        👇 Quyidagilardan birini tanlang
                        """
        );
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }

    public static SendMessage checkAnswerMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """      
                        📩 Test javoblarini quyidagi ko'rinishda yuboring!
                                                
                        Test kodi # O'z javoblaringiz
                                                                                                
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setParseMode("markdown");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage checkFirstCompulsoryBlockTestAnswerMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """      
                        📩1-majburiy fan javoblarini quyidagi ko'rinishda yuboring!
                                                
                        Blok test kodi # O'z javoblaringiz
                                                
                        (Har bir fan javoblarini kiritganda blok test kodiga e'tibor bering, u shu blok test yakunlanguncha o'zgarmaydi)
                                                                                               
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setParseMode("markdown");
        return sendMessage;
    }

    public static SendMessage checkSecondCompulsoryBlockTestAnswerMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """      
                        📩2-majburiy fan javoblarini quyidagi ko'rinishda yuboring!
                                                
                        Blok test kodi # O'z javoblaringiz
                                                
                        (Har bir fan javoblarini kiritganda blok test kodiga e'tibor bering, u shu blok test yakunlanguncha o'zgarmaydi)
                                                                                                
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setParseMode("markdown");
        return sendMessage;
    }

    public static SendMessage checkThirdCompulsoryBlockTestAnswerMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """      
                        📩3-majburiy fan javoblarini quyidagi ko'rinishda yuboring!
                                                
                        Blok test kodi # O'z javoblaringiz
                                                          
                        (Har bir fan javoblarini kiritganda blok test kodiga e'tibor bering, u shu blok test yakunlanguncha o'zgarmaydi)
                                      
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setParseMode("markdown");
        return sendMessage;
    }

    public static SendMessage checkFirstMajorBlockTestAnswerMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """      
                        📩1-asosiy fan javoblarini quyidagi ko'rinishda yuboring!
                                                
                        Blok test kodi # O'z javoblaringiz
                                                
                        (Har bir fan javoblarini kiritganda blok test kodiga e'tibor bering, u shu blok test yakunlanguncha o'zgarmaydi)
                                                                                                
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setParseMode("markdown");
        return sendMessage;
    }

    public static SendMessage checkSecondMajorBlockTestAnswerMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """      
                        📩2-asosiy fan javoblarini quyidagi ko'rinishda yuboring!
                                                
                        Blok test kodi # O'z javoblaringiz
                                                
                        (Har bir fan javoblarini kiritganda blok test kodiga e'tibor bering, u shu blok test yakunlanguncha o'zgarmaydi)
                                                                                                
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setParseMode("markdown");
        return sendMessage;
    }

    public static SendMessage checkTestAnswerErrorMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """    
                        ❗Hurmatli foydalanuvchi
                        📩 Test javoblarini quyidagi ko'rinishda yuboring!
                                                
                        Test kodi # O'z javoblaringiz
                                                                                                
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                                                
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                        """
        );
        sendMessage.setParseMode("markdown");
        return sendMessage;
    }

    public static SendMessage testAnswersAreIncomplete(String chatId, Test test) {
        return new SendMessage(
                chatId,
                """
                        ❗%d kodli testda savollar soni %d ta
                        Javoblaringiz sonini tekshiring
                        """.formatted(test.getId(), test.getKeys().length())
        );
    }

    public static SendMessage blockTestAnswersAreIncomplete(String chatId, BlocTest blockTest, Test test) {
        return new SendMessage(
                chatId,
                """
                        ❗%d kodli %s testida savollar soni %d ta
                        Javoblaringiz sonini tekshiring
                        """.formatted(blockTest.getId(), test.getSubjectName(), test.getKeys().length())
        );
    }
    public static SendMessage testCodeErrorMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        ⛔ Test bazadan topilmadi.
                        Test kodini to'g'ri kiriting!
                        """
        );
    }

    public static SendMessage testResultMessage(String chatId, int correctAnswer, Test test, int count, String name) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formattedDateTime = LocalDateTime.now().format(formatter);
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        👤 Foydalanuvchi:
                        %s
                        📚 Fan: %s
                        📖 Test kodi: %d
                        ✏️ Jami savollar soni: %d ta
                        ✅ To'g'ri javoblar soni: %d ta
                        🔣 Foiz : %d %%
                        ☝️ Noto`g`ri javoblaringiz test yakunlangandan so'ng yuboriladi.
                        -------------------------------------------------
                        🕐 Sana, vaqt: %s
                        """.formatted(name, test.getSubjectName(), test.getId(), test.getKeys().length(), count, correctAnswer, formattedDateTime)
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }

    public static SendMessage blockTestResultMessage(String chatId, BlocTest blocTest, List<Test> tests, BlocTestSubmission blocTestSubmission, float totalScore, int totalNumberOfQuestions, int numberOfCorrectAnswer, String name) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formattedDateTime = LocalDateTime.now().format(formatter);
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        👤 Foydalanuvchi:
                        %s
                        📗 Ona tili:   %s ta to'g'ri (%.1f ball)
                        📗 Matematika: %s ta to'g'ri (%.1f ball)
                        📗 Tarix:      %s ta to'g'ri (%.1f ball)
                                                
                        📚 %s: %s ta to'g'ri (%.1f ball)
                        📚 %s: %s ta to'g'ri (%.1f ball)
                        📖 Test kodi: %d
                        ✏️ Jami savollar soni: %d ta
                        ✅ To'g'ri javoblar soni: %d ta
                        🔣 Jami to'plangan ball : %.1f ball
                        ☝️ Noto`g`ri javoblaringiz test yakunlangandan so'ng yuboriladi.
                        -------------------------------------------------
                        🕐 Sana, vaqt: %s
                        """.formatted(name, blocTestSubmission.getFirstCompulsorySubjectCorrectAnswersCount(),
                        1.1f * blocTestSubmission.getFirstCompulsorySubjectCorrectAnswersCount(),
                        blocTestSubmission.getSecondCompulsorySubjectCorrectAnswersCount(),
                        1.1f * blocTestSubmission.getSecondCompulsorySubjectCorrectAnswersCount(),
                        blocTestSubmission.getThirdCompulsorySubjectCorrectAnswersCount(),
                        1.1f * blocTestSubmission.getThirdCompulsorySubjectCorrectAnswersCount(),
                        tests.get(3).getSubjectName(), blocTestSubmission.getFirstMajorSubjectCorrectAnswersCount(),
                        3.1f * blocTestSubmission.getFirstMajorSubjectCorrectAnswersCount(),
                        tests.get(4).getSubjectName(), blocTestSubmission.getSecondMajorSubjectCorrectAnswersCount(),
                        2.1f * blocTestSubmission.getSecondMajorSubjectCorrectAnswersCount(), blocTest.getId(), totalNumberOfQuestions, numberOfCorrectAnswer,
                        totalScore, formattedDateTime)
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }

    public static SendMessage currentStatusOfTestMessage(String ownerId, List<TestSubmission> submissions, Test test) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("\uD83D\uDCD4Hozirgi holat\n\n")
                .append("📖 Test kodi: ")
                .append(test.getId()).append("\n")
                .append("📚 Fan: ")
                .append(test.getSubjectName()).append("\n")
                .append("✏️Savollar soni: ")
                .append(test.getKeys().length()).append("\n\n");
        for (TestSubmission submission : submissions) {
            User user = submission.getUser();
            messageBuilder.append(String.format("⏺ %s: %d tadan %d ta topdi\n",
                    user.getFullName(),
                    submission.getTotalAnswersCount(),
                    submission.getCorrectAnswersCount()));
        }
        return new SendMessage(ownerId, messageBuilder.toString());
    }

    public static SendMessage currentStatusOfBlockTestMessage(String ownerId, List<BlocTestSubmission> blocTestSubmissions, List<Test> tests, BlocTest blockTest, int totalNumberOfQuestions) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("\uD83D\uDCD4Hozirgi holat\n\n")
                .append("📖 Test kodi: ")
                .append(blockTest.getId()).append("\n")
                .append("📚 Blok: ")
                .append(tests.get(3).getSubjectName()).append(" va ")
                .append(tests.get(4).getSubjectName()).append("\n")
                .append("✏️Savollar soni: ")
                .append(totalNumberOfQuestions).append("ta \n\n");
        for (BlocTestSubmission blocTestSubmission : blocTestSubmissions) {
            User user = blocTestSubmission.getUser();
            messageBuilder.append(String.format(
                    "⏺ %s: \n Ona tili: %d ta topdi\n Matemetika: %d ta topdi\n Tarix %d ta topdi\n %s: %d ta topdi\n %s: %d ta topdi \n\n",
                    user.getFullName(),
                    blocTestSubmission.getFirstCompulsorySubjectCorrectAnswersCount(),
                    blocTestSubmission.getSecondCompulsorySubjectCorrectAnswersCount(),
                    blocTestSubmission.getThirdCompulsorySubjectCorrectAnswersCount(),
                    tests.get(3).getSubjectName(),
                    blocTestSubmission.getFirstMajorSubjectCorrectAnswersCount(),
                    tests.get(4).getSubjectName(),
                    blocTestSubmission.getSecondMajorSubjectCorrectAnswersCount()));
        }
        return new SendMessage(ownerId, messageBuilder.toString());
    }
    public static SendMessage testOwnerResultMessage(String ownerId, Test test, String name) {
        SendMessage sendMessage = new SendMessage(
                ownerId,
                """
                        🟢 %s  %s fanidan
                        %d kodli testning javoblarini yubordi.
                        """.formatted(name, test.getSubjectName(), test.getId())
        );
        sendMessage.setReplyMarkup(testCaseControlButton(test));
        return sendMessage;
    }

    public static SendMessage blockTestOwnerResultMessage(String ownerId, BlocTest blocTest, String name) {
        SendMessage sendMessage = new SendMessage(
                ownerId,
                """
                        🟢 %s  blok testdan
                        %d kodli testning javoblarini yubordi.
                        """.formatted(name, blocTest.getId())
        );
        sendMessage.setReplyMarkup(blockTestCaseControlButton(blocTest));
        return sendMessage;
    }

    public static SendMessage canBeCheckedOnceMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        Siz oldin bu testga javoblaringizni
                        yuborgansiz.
                        😎 Har bir testga bir marotaba javob
                        yuborish mumkin.
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }


    public static SendMessage testFinishedMessageForUsers(String userId, TestSubmission submission, Test test) {
        return new SendMessage(
                userId,
                """
                        🏁 %d kodli test yakunlandi!
                        📚Fan nomi: %s
                        ❌ Sizning noto'g'ri javoblaringiz:
                        %s
                        """.formatted(test.getId(), test.getSubjectName(), submission.getWrongAnswers())
        );
    }

    public static SendMessage blockTestFinishedMessageForUsers(String userId, BlocTestSubmission blockTestSubmission, BlocTest blockTest, List<Test> tests) {
        return new SendMessage(
                userId,
                """
                        🏁 %d kodli test yakunlandi!
                        📚Asosiy fanlar nomi: %s va %s
                        ❌ Sizning noto'g'ri javoblaringiz:
                        Ona tili: %s
                        Matemetik: %s
                        Tarix: %s
                        %s: %s
                        %s: %s
                        """.formatted(blockTest.getId(), tests.get(3).getSubjectName(), tests.get(4).getSubjectName(),
                        blockTestSubmission.getFirstCompulsorySubjectWrongAnswers(),
                        blockTestSubmission.getSecondCompulsorySubjectWrongAnswers(),
                        blockTestSubmission.getThirdCompulsorySubjectWrongAnswers(),
                        tests.get(3).getSubjectName(), blockTestSubmission.getFirstMajorSubjectWrongAnswers(),
                        tests.get(4).getSubjectName(), blockTestSubmission.getSecondMajorSubjectWrongAnswers())
        );
    }

    public static SendMessage testFinishedMessageForTestOwner(String ownerId, Test test, String keys, String result) {
        return new SendMessage(
                ownerId,
                """
                        #Natijalar_%d
                        #%s
                                                
                        🔐Test yakunlandi.
                                                
                        Fan: %s
                        Test kodi: %d
                        Savollar soni: %d ta
                                                
                        ✅ Natijalar:
                                                
                        %s
                                                
                        To`g`ri javoblar:
                        %s
                                                
                        Testda qatnashgan barcha ishtirokchilarga minnatdorchilik bildiramiz. Bilimingiz ziyoda bo’lsin!!☺️
                        Shu kabi testlarni topish uchun @mathtest_online va @pmtestbaza kanallarini kuzatib boring:)🧑🏻‍🎓👩🏻‍🎓
                        """.formatted(test.getId(), test.getSubjectName(), test.getSubjectName(), test.getId(), test.getKeys().length(), result, keys)
        );
    }

    public static SendMessage blockTestFinishedMessageForTestOwner(String ownerId, BlocTest blockTest, List<Test> tests, int totalNumberOfQuestions, String firstCompulsorySubjectKeys,
                                                                   String secondCompulsorySubjectKeys, String thirdCompulsorySubjectKeys, String firstMajorSubjectKeys,
                                                                   String secondMajorSubjectKeys, String result) {
        return new SendMessage(
                ownerId,
                """
                        #Natijalar_%d
                        #%s va %s
                                                
                        🔐Test yakunlandi.
                                                
                        Blok: %s va %s
                        Test kodi: %d
                        Savollar soni: %d ta
                                                
                        ✅ Natijalar:
                                                
                        %s
                                                
                                                
                        To`g`ri javoblar:
                        Ona tili: %s
                                                
                        Matemetika: %s
                                                
                        Tarix: %s
                                                
                        %s: %s
                                                
                        %s: %s
                        Testda qatnashgan barcha ishtirokchilarga minnatdorchilik bildiramiz. Bilimingiz ziyoda bo’lsin!!☺️
                        Shu kabi testlarni topish uchun @mathtest_online va @pmtestbaza kanallarini kuzatib boring:)🧑🏻‍🎓👩🏻‍🎓
                        """.formatted(blockTest.getId(), tests.get(3).getSubjectName(), tests.get(4).getSubjectName(),
                        tests.get(3).getSubjectName(), tests.get(4).getSubjectName(), blockTest.getId(), totalNumberOfQuestions,
                        result, firstCompulsorySubjectKeys, secondCompulsorySubjectKeys, thirdCompulsorySubjectKeys, tests.get(3).getSubjectName(),
                        firstMajorSubjectKeys, tests.get(4).getSubjectName(), secondMajorSubjectKeys)
        );
    }

    public static SendMessage testCompletedMessage(String chatId) {
        return new SendMessage(
                chatId,
                "⛔Bu test yakunlangan."
        );
    }

    public static SendMessage helpMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        Botdan foydalanishda ishlatiladigan buyruqlar haqida qisqacha.
                                                
                        /start - Foydalanishni boshlash yoki qayta boshlash uchun.
                                                
                        📗Oddiy test - 200 tadan ko'p bo'lmagan istalgancha test javoblarini jo'natish orqali testingizni saqlab qo'yishingiz mumkin.
                                                
                        📚Blok test - Blok test to'g'ri javoblarini saqlab qo'yishingiz mumkin.
                        Bunda Majburiy fanlar(3 ta) va Asosiy fanlar(2 ta) bo'ladi.
                                                
                        ✅Oddiy testni tekshirish - O'z javoblaringizni saqlab qo'yilgan oddiy test bilan solishtirib natijangizni bilishingiz mumkin.

                        ☑️Blok testni tekshirish - O'z javoblaringizni saqlab qo'yilgan Blok test bilan solishtirib natijangizni bilishingiz mumkin.
                        Bunda har bir fan uchun a'lohida javob yo'llaysiz.
                                                
                        /yordam - Botdan foydalanish yo'riqnomasi bilan tanishingiz mumkin.4
                                                
                        /ismni_yangilash - Foydalanish boshlangan vaqtda kiritgan ism va familyangizni yangilashingiz mumkin.
                                                
                        Qo'shimcha ma'lumot yoki savollar uchun @SurojbekRozzoqov ga murojaat qilishingiz mumkin.
                        """
        );
    }

    public static SendMessage changeNameMessage(String chatId, User user) {
        return new SendMessage(
                chatId,
                """
                        🔄️Hozirda botdagi to'liq ismingiz %s kabi saqlangan, agar ushbu ma'lumot noto'g'ri bo'lsa unda to'g'ri ma'lumotni kiriting.
                        """.formatted(user.getFullName())
        );
    }

    public static SendMessage developerUsernameMessage(String chatId) {
        return new SendMessage(chatId, "\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBB @sh_rozzoqov");
    }

    public static SendMessage fullNameChangeCompleteMessage(String chatId, String text, User user) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ✅ To'liq ismingiz %s dan *%s*ga o'zgartirildi.
                        """.formatted(user.getFullName(), text)
        );
        sendMessage.setParseMode("markdown");
        return sendMessage;
    }

    public static ReplyKeyboardMarkup getMainButtons() {
        KeyboardButton createTest = new KeyboardButton("➕ Test yaratish");
        KeyboardButton checkAnswers = new KeyboardButton("✅ Javoblarni tekshirish");
        KeyboardRow row = new KeyboardRow(List.of(createTest, checkAnswers));
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(row));
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup createTestButton() {
        KeyboardButton simpleTestButton = new KeyboardButton("📗Oddiy test");
        KeyboardButton blocTestButton = new KeyboardButton("📚Blok test");
        return getReplyKeyboardMarkup(simpleTestButton, blocTestButton);
    }

    public static ReplyKeyboardMarkup checkTestButton() {
        KeyboardButton simpleTestButton = new KeyboardButton("✅Oddiy testni tekshirish");
        KeyboardButton blocTestButton = new KeyboardButton("☑️Blok testni tekshirish");
        return getReplyKeyboardMarkup(simpleTestButton, blocTestButton);
    }

    private static ReplyKeyboardMarkup getReplyKeyboardMarkup(KeyboardButton simpleTestButton, KeyboardButton blocTestButton) {
        KeyboardButton keyboardButton = new KeyboardButton("🔙 Orqaga qaytish");
        KeyboardRow firstRow = new KeyboardRow(List.of(simpleTestButton, blocTestButton));
        KeyboardRow secondRow = new KeyboardRow(List.of(keyboardButton));
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(firstRow, secondRow));
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup goBackButton() {
        KeyboardButton keyboardButton = new KeyboardButton("🔙 Orqaga qaytish");
        KeyboardRow row = new KeyboardRow(List.of(keyboardButton));
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(row));
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public static InlineKeyboardMarkup createSubscriptionKeyboardMessage() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        List<InlineKeyboardButton> thirdRow = new ArrayList<>();

        InlineKeyboardButton joinFirstChannel = new InlineKeyboardButton("➕️ Kanalga o'tish");
        joinFirstChannel.setUrl("https://t.me/pmtestbaza");

        InlineKeyboardButton joinSecondChannel = new InlineKeyboardButton("➕️ Kanalga o'tish");
        joinSecondChannel.setUrl("https://t.me/mathtest_online");

        InlineKeyboardButton becomeMember = new InlineKeyboardButton("✅ A'zo bo'ldim");
        becomeMember.setCallbackData("unsubscribed");

        firstRow.add(joinFirstChannel);
        secondRow.add(joinSecondChannel);
        thirdRow.add(becomeMember);

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }


    public static InlineKeyboardMarkup testCaseControlButton(Test test) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        InlineKeyboardButton currentSituation = new InlineKeyboardButton("\uD83D\uDCD4Hozirgi holat");
        currentSituation.setCallbackData("currentStatusForTest%d".formatted(test.getId()));
        currentSituation.setSwitchInlineQuery(test.getId().toString());
        InlineKeyboardButton becomeMember = new InlineKeyboardButton("\uD83D\uDD1ATestni yakunlash");
        becomeMember.setCallbackData("completeTest%d".formatted(test.getId()));

        firstRow.add(currentSituation);
        secondRow.add(becomeMember);

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup blockTestCaseControlButton(BlocTest test) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        InlineKeyboardButton currentSituation = new InlineKeyboardButton("\uD83D\uDCD4Hozirgi holat");
        currentSituation.setCallbackData("currentStatusForBlockTest%d".formatted(test.getId()));
        currentSituation.setSwitchInlineQuery(test.getId().toString());
        InlineKeyboardButton becomeMember = new InlineKeyboardButton("\uD83D\uDD1ATestni yakunlash");
        becomeMember.setCallbackData("completeBlockTest%d".formatted(test.getId()));

        firstRow.add(currentSituation);
        secondRow.add(becomeMember);

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public static SendMessage blockTestCodeIsIncorrect(String chatId, Integer id) {
        return new SendMessage(
                chatId,
                """
                        🙅🏻‍♂️Hurmatli foydalanuvchi siz %d kodli blok testni tekshirayabsiz.
                        O'z javoblaringizni quyidagi ko'rinishda yuborishingiz kerak!
                                                
                        Blok test kodi # O'z javoblaringiz
                                                                                                
                        Misol:
                        %d#abcdac...
                        yoki
                        %d#1a2b3c4d5a6c...
                        """.formatted(id, id, id)
        );
    }
}