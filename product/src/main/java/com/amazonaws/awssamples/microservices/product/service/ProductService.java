package com.amazonaws.awssamples.microservices.product.service;

import com.amazonaws.awssamples.microservices.common.Product;
import com.amazonaws.awssamples.microservices.common.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Product Service API
 */
@RestController
@RequestMapping(value="/service/product")
public class ProductService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${REVIEW_URL}")
    private String reviewUrl;

    private Map<Long, Product> productMap;

    public ProductService() {
        this.productMap = new HashMap<>();
        this.productMap.put(1L, new Product(1, "Book1111", ""));
        this.productMap.put(2L, new Product(2, "Book2222", ""));
        this.productMap.put(3L, new Product(3, "Book3333", ""));
        this.productMap.put(4L, new Product(4, "Book4444", ""));
    }

    /**
     * Simply return the Product object with no review
     *
     * @param id
     * @return
     */
    @RequestMapping(value="/v1/{id}",method = RequestMethod.GET)
    public Product getProduct(@PathVariable Long id){
        return productMap.get(id);
    }

    /**
     * Doc Reference to propagate the bearer token
     * - https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/bearer-tokens.html
     * @param id
     * @return
     */
    @RequestMapping(value="/v2/{id}",method = RequestMethod.GET)
    public Product getProductWithReview(@PathVariable Long id){
        Product product = productMap.get(id);
        restTemplate.getInterceptors().add((request, body, execution) -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication == null) {
                        return execution.execute(request, body);
                    }

                    if (!(authentication.getCredentials() instanceof AbstractOAuth2Token)) {
                        return execution.execute(request, body);
                    }

                    AbstractOAuth2Token token = (AbstractOAuth2Token) authentication.getCredentials();
                    if (token instanceof Jwt) {
                        Jwt jwtToken = (Jwt)token;
                        Map<String, Object> claims = jwtToken.getClaims();
                        System.out.println("User name: " + claims.get("username"));
                    }

                    request.getHeaders().setBearerAuth(token.getTokenValue());
                    return execution.execute(request, body);
                });
        Review review = restTemplate.getForObject(reviewUrl + "/service/review/v1/" + id, Review.class);
        product.setReview(review.getReview());
        productMap.put(id, product);
        System.out.println("here now");
        return product;
    }

}
