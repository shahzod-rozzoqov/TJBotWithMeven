package uz.shoh.tjbot.message;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.shoh.tjbot.entitys.test.entity.Test;
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
                         ✏️ Familiya ismingizni quyidagi usulda kiriting:
                         
                         fi-Familiya Ism
                         
                         Misol:
                         fi-Eshmatov Toshmat
                        """);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        return sendMessage;
    }

    public static SendMessage checkFullNameMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                         ❗ Hurmatli foydalanuvchi familiya ismingizni
                         quyidagi usulda kiriting:
                         
                         fi-Familiya Ism
                         
                         Misol:
                         fi-Eshmatov Toshmat
                        """);
    }

    public static SendMessage registrationBotMessage(String chatId, boolean isSubscribed) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        🤝 Ro'yhatdan o'tganingiz uchun raxmat.
                        Botimizdan to'liq foydalanishingiz uchun
                        @pmtestbaza va @online_mathtest kanallariga
                        obuna bo'lishingiz kerak!
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

    public static SendMessage handleSubscribedActionMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        💯 Botimizdan to'liq foydalanishingiz mumkin.
                        """
        );
        sendMessage.setReplyMarkup(getMainButtons());
        return sendMessage;
    }

    public static SendMessage createTestMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        📝 Test yaratish uchun xabarni
                                        
                        +test_Fan nomi_To'g'ri javoblar
                                        
                        ko'rinishida yuboring.
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                                        
                        Misol:
                        +test_Matematika_abccd...
                        yoki
                        +test_Matematika_1a2b3c4c5d...
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage createTestErrorMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ❗Hurmatli foydalanuvchi
                        Test yaratish uchun xabarni
                                        
                        +test_Fan nomi_To'g'ri javoblar
                                        
                        ko'rinishida yuboring.
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                                        
                        Misol:
                        +test_Matematika_abccd...
                        yoki
                        +test_Matematika_1a2b3c4c5d...
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage secondCreateTestErrorMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ❗Hurmatli foydalanuvchi
                        Test yaratishda Fan nomi va To'g'ri
                        javoblarga e'tiborli bo'ling ular bo'sh
                        holda bo'lishi mumkin emas
                                                
                        Xabarni
                                        
                        +test_Fan nomi_To'g'ri javoblar
                                        
                        ko'rinishida yuboring.
                                        
                        Misol:
                        +test_Matematika_abccd...
                        yoki
                        +test_Matematika_1a2b3c4c5d...
                        """
        );
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage generatedTestMessage(String chatId, Test test) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """
                        ✅ Test bazaga qo'shildi
                                                
                        🔢Test kodi: %d
                        📚Fan nomi: %s
                        ✍️Savollar soni: %d
                                                
                        Testda qatnashganlar o'z javoblarini quyidagi
                        ko'rinishida yuborishlari mumkin:
                                                
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
                        📩 Test javoblarini quyidagi ko'rinishda
                        yuboring!
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                                                
                        Test kodi # O'z javoblaringiz
                                                                                                
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                        """
        );
        sendMessage.setParseMode("markdown");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage checkAnswerErrorMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(
                chatId,
                """    
                        ❗Hurmatli foydalanuvchi
                        📩 Test javoblarini quyidagi ko'rinishda
                        yuboring!
                        (⚠️Ko'rsatilgan ko'rinishda yubormasangiz
                        javoblarni tekshirishda xatoliklar bo'lishi mumkin)
                                                
                        Test kodi # O'z javoblaringiz
                                                                                                
                        Misol:
                        *123#abcdac...
                        yoki
                        123#1a2b3c4d5a6c...*
                        """
        );
        sendMessage.setParseMode("markdown");
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sendMessage.setReplyMarkup(goBackButton());
        return sendMessage;
    }

    public static SendMessage checkAnswerErrorMessage(String chatId, Test test) {
        return new SendMessage(
                chatId,
                """
                        ❗%d kodli testda savollar soni %d ta
                        Javoblaringiz sonini tekshiring
                        """.formatted(test.getId(), test.getKeys().length())
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
        return new SendMessage(
                chatId,
                """
                        👤 Foydalanuvchi:
                        %s
                        📚 Fan: %s
                        📖 Test kodi: %d
                        ✏️ Jami savollar soni: %d ta
                        ✅ To'g'ri javoblar soni: %d ta
                        🔣 Foiz : %d %%
                        ☝️ Noto`g`ri javoblaringiz test yakunlangandan
                        so'ng yuboriladi.
                        -------------------------------------------------
                        🕐 Sana, vaqt: %s
                        """.formatted(name, test.getSubjectName(), test.getId(), test.getKeys().length(), count, correctAnswer, formattedDateTime)
        );
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

    public static SendMessage canBeCheckedOnceMessage(String chatId) {
        return new SendMessage(
                chatId,
                """
                        Siz oldin bu testga javoblaringizni
                        yuborgansiz.
                        😎 Har bir testga bir marotaba javob
                        yuborish mumkin.
                        """
        );
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
                        """.formatted(test.getId(), test.getSubjectName(), test.getSubjectName(), test.getId(), test.getKeys().length(), result, keys)
        );
    }

    public static SendMessage testCompletedMessage(String chatId) {
        return new SendMessage(
                chatId,
                "⛔Bu test yakunlangan."
        );
    }

    public static SendMessage helpMessage(String chatId) {
        //sendMessage.setParseMode("markdown");
        return new SendMessage(
                chatId,
                """
                        Botdan foydalanishda ishlatiladigan buyruqlar haqida qisqacha
                                                
                        /start - Foydalanishni boshlash yoki qayta boshlash uchun
                                                
                        ➕ Test yaratish - O'z testingiz to'g'ri javoblarini saqlab
                        qo'yishingiz mumkin.
                        ✅ Javoblarni tekshirish - O'z javoblaringizni
                        saqlab qo'yilgan test bilan solishtirib natijangizni bilishingiz mumkin.
                                                
                        /yordam - Botdan foydalanish yo'riqnomasi bilan tanishingiz mumkin.
                        /ismni_yangilash - Foydalanish boshlangan vaqtda kiritgan ism va familyangizni yangilashingiz mumkin.
                                                
                        Qo'shimcha ma'lumot yoki savollar uchun @RozzoqovSurojbek ga murojaat qilishingiz mumkin.
                        """
        );
    }

    public static SendMessage changeNameMessage(String chatId, User user) {
        return new SendMessage(
                chatId,
                """
                        Hozirda botdagi to'liq ismingiz %s kabi saqlangan, agar ushbu ma'lumot noto'g'ri bo'lsa unda to'g'ri ma'lumotni kiriting.
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
        joinSecondChannel.setUrl("https://t.me/online_mathtest");
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
        currentSituation.setCallbackData("currentSituation%d".formatted(test.getId()));
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
}



