package start;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by egor on 23.02.17.
 */
@RestController
@RequestMapping("/api/")
public class UserController {

    @NotNull
    private final AccountService accountService;

    /**
     * Данный метод вызывается с помощью reflection'a, поэтому Spring позволяет инжектить в него аргументы.
     * Подробнее можно почитать в сорцах к аннотации {@link RequestMapping}. Там описано как заинжектить различные атрибуты http-запроса.
     * Возвращаемое значение можно так же варьировать. Н.п. Если отдать InputStream, можно стримить музыку или видео
     */
    //TODO: Method "register" -   Обработать 500 ошибки. К примеру, если прислать неполные данные в json, то вылетит 500 ошибка.
    @RequestMapping(path = "/register", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public String register(@RequestBody UserProfile userProfile, HttpSession httpSession) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JSONObject resultJson = new JSONObject();
        JSONArray errorsJson = new JSONArray();

        if (StringUtils.isEmpty(userProfile.getName())) {
            errorsJson.put("Поле 'имя' пустое. Заполните его.");
        }
        if (StringUtils.isEmpty(userProfile.getPassword())) {
            errorsJson.put("Поле \"пароль\" пустое. Заполните его.");
        }
        if (StringUtils.isEmpty(userProfile.getEmail())) {
            errorsJson.put((String)"Поле \"email\" пустое. Заполните его.");
        }
        if (StringUtils.isEmpty(userProfile.getNick())) {
            errorsJson.put("Поле \"ник\" пустое. Заполните его.");
        }

        if (errorsJson.length() != 0) {
            resultJson.put("error", errorsJson);
            return resultJson.toString();
        }

        userProfile = accountService.register(userProfile);

        if (userProfile != null) {
//            httpSession.setAttribute("email", userProfile.getEmail());
            return gson.toJson(userProfile);
        } else {
            return resultJson.put("error", "Такой пользователь уже существует").toString();
        }


//        if (httpSession.getAttribute(email) != null) {
//            return gson.toJson(userProfile);
//        } else {
//            httpSession.setAttribute(email, true);
//            return gson.toJson(userProfile);
//        }


    }

    @RequestMapping(path = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public String login(@RequestBody UserProfile userProfile, HttpSession httpSession) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String email = userProfile.getEmail();
        String password = userProfile.getPassword();

        if (password.isEmpty()) {
            return gson.toJson("Поле \"пароль\" пустое. Заполните его.");
        }
        if (email.isEmpty()) {
            return gson.toJson("Поле \"email\" пустое. Заполните его.");
        }

        if (accountService.login(email, password)) {
            userProfile = accountService.getUser(email);
            httpSession.setAttribute("email", email);
            return gson.toJson(userProfile);
        } else {
            return gson.toJson("\"result\" : \"Неверное имя пользователя или пароль\"");
        }
    }

    @RequestMapping(path = "/currentsessionuser", method = RequestMethod.GET)
    public String currentSessionUser(@RequestParam("email") String email, HttpSession httpSession) {
//        String email = httpServletRequest.getParameter("email");
        JSONObject resultJson = new JSONObject();
        return resultJson.put("currentSessionUser", accountService.getUserOfCurrentSession(httpSession).getEmail()).toString();
    }



    /**
     * Конструктор тоже будет вызван с помощью reflection'а. Другими словами, объект создается через ApplicationContext.
     * Поэтому в нем можно использовать DI. Подробнее про это расскажу на лекции.
     */
    public UserController(@NotNull AccountService accountService) {
        this.accountService = accountService;
    }


//    private static final class GetMsgRequest {
//        int userId;
//
//        @JsonCreator
//        GetMsgRequest(@JsonProperty("userId") int userId) {
//            this.userId = userId;
//        }
//
//        public int getUserId() {
//            return userId;
//        }
//    }
}
