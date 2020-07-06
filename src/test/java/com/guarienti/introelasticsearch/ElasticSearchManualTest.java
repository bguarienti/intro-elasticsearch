package com.guarienti.introelasticsearch;

import com.guarienti.introelasticsearch.model.Article;
import com.guarienti.introelasticsearch.model.Author;
import com.guarienti.introelasticsearch.repository.ArticleRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import static java.util.Arrays.asList;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@SpringBootTest
public class ElasticSearchManualTest {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ArticleRepository articleRepository;

    private final Author johnSmith = new Author("John Smith");
    private final Author johnDoe = new Author("John Doe");

    @BeforeEach
    public void before() {
        Article article = new Article("Spring Data Elasticsearch");
        article.setAuthors(asList(johnSmith, johnDoe));
        articleRepository.save(article);

        article = new Article("What should we practice next?");
        article.setAuthors(asList(johnDoe));
        articleRepository.save(article);
    }

    @AfterEach
    public void after() {
        articleRepository.deleteAll();
    }

    @Test
    public void givenArticle_whenSaved_thenIdIsAssigned() {
        Article article = new Article("Testing is great!");
        article.setAuthors(asList(johnSmith));

        article = articleRepository.save(article);

        assertNotNull("ID should be assigned", article.getId());
    }

    @Test
    public void givenPersistedArticles_whenSearchByAuthorsName_thenRightFound(){
        final Page<Article> articleByAuthorName = articleRepository.findByAuthorsName(johnDoe.getName(), PageRequest.of(0, 10));
        assertEquals("Amount of records should be 2", 2L, articleByAuthorName.getTotalElements());
    }

    @Test
    public void givenCustomQuery_whenSearchByAuthorsName_thenArticleIsFound(){
        final Page<Article> articleByAuthorName = articleRepository.findByAuthorsNameUsingCustomQuery(johnDoe.getName(), PageRequest.of(0, 10));
        assertEquals("Amount of records should be 2", 2L, articleByAuthorName.getTotalElements());
    }

    @Test
    public void givenPersistedArticles_whenSearchByRegex_thenArticleIsFound(){
        final Query searchQuery = new NativeSearchQueryBuilder().withFilter(regexpQuery("title", ".*practice.*"))
                .build();
        SearchHits<Article> articles = elasticsearchRestTemplate.search(searchQuery, Article.class, IndexCoordinates.of("blog"));

        assertEquals("Amount of records should be 1", 1L, articles.getTotalHits());
    }

}
