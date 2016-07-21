package sample;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

import org.hamcrest.Matcher;
import org.junit.runner.RunWith;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.*;
import org.springframework.web.util.*;

import sample.context.Timestamper;
import sample.model.*;

/**
 * Spring コンテナを用いた Web 検証サポートクラス。
 * <p>Controller に対する URL 検証はこちらを利用して下さい。
 * <p>本クラスを継承したテストクラスを作成後、以下のアノテーションを付与してください。
 * <pre>
 *  {@literal @}RunWith(SpringRunner.class)
 *  {@literal @}WebMvcTest([テスト対象クラス].class)
 * </pre>
 * <p>{@literal @}WebMvcTest 利用時は標準で {@literal @}Component や {@literal @}Service 等の
 * コンポーネントはインスタンス化されないため、必要に応じて {@literal @}MockBean などを利用して代替するようにしてください。
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("testweb")
public abstract class WebTestSupport {

    protected static final Logger logger = LoggerFactory.getLogger("ControllerTest");

    @Autowired
    protected MockMvc mvc;
    
    protected Timestamper mockTime;
    protected BusinessDayHandler businessDay;
    protected DataFixtures fixtures;
        
    public WebTestSupport() {
        this.mockTime = new Timestamper();
        this.businessDay = new BusinessDayHandler();
        this.businessDay.setTime(mockTime);
        this.fixtures = new DataFixtures();
        this.fixtures.setTime(mockTime);
        this.fixtures.setBusinessDay(businessDay);
    }
    
    protected String uri(String path) {
        return prefix() + path;
    }
    
    protected UriComponentsBuilder uriBuilder(String path) {
        return UriComponentsBuilder.fromUriString(uri(path));
    }

    protected String prefix() {
        return "/";
    }

    /** Get 要求を投げて結果を検証します。 */
    protected ResultActions performGet(String path, final JsonExpects expects) {
        return performGet(uriBuilder(path).build(), expects);
    }
    
    protected ResultActions performGet(UriComponents uri, final JsonExpects expects) {
        return perform(
                get(uri.toUriString()).accept(MediaType.APPLICATION_JSON),
                expects.expects.toArray(new ResultMatcher[0]));
    }
    
    /** Post 要求を投げて結果を検証します。 */
    protected ResultActions performPost(String path, final JsonExpects expects) {
        return performPost(uriBuilder(path).build(), expects);
    }
    
    protected ResultActions performPost(UriComponents uri, final JsonExpects expects) {
        return perform(
                post(uri.toUriString()).accept(MediaType.APPLICATION_JSON),
                expects.expects.toArray(new ResultMatcher[0]));
    }
    
    protected ResultActions perform(final RequestBuilder req, final ResultMatcher... expects) {
        try {
            ResultActions result = mvc.perform(req);
            for (ResultMatcher matcher : expects) {
                result.andExpect(matcher);
            }
            return result;
        } catch (Exception e) {
            throw new InvocationException(e);
        }        
    }


    /** JSON 検証をビルダー形式で可能にします */
    public static class JsonExpects {
        public List<ResultMatcher> expects = new ArrayList<>();
        public JsonExpects match(String key, Object expectedValue) {
            this.expects.add(jsonPath(key).value(expectedValue));
            return this;
        }
        public <T> JsonExpects matcher(String key, Matcher<T> matcher) {
            this.expects.add(jsonPath(key).value(matcher));
            return this;
        }
        public JsonExpects empty(String key) {
            this.expects.add(jsonPath(key).isEmpty());
            return this;
        }
        public JsonExpects notEmpty(String key) {
            this.expects.add(jsonPath(key).isNotEmpty());
            return this;
        }
        public JsonExpects array(String key) {
            this.expects.add(jsonPath(key).isArray());
            return this;
        }
        public JsonExpects map(String key) {
            this.expects.add(jsonPath(key).isMap());
            return this;
        }
        // 200 OK
        public static JsonExpects success() {
            JsonExpects v = new JsonExpects();
            v.expects.add(status().isOk());
            return v;
        }
        // 400 Bad Request
        public static JsonExpects failure() {
            JsonExpects v = new JsonExpects();
            v.expects.add(status().isBadRequest());
            return v;
        }
    }
    
}
