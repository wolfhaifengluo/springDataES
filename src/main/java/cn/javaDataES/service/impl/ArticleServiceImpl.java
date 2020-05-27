package cn.javaDataES.service.impl;

import cn.javaDataES.dao.IArticleDao;
import cn.javaDataES.domain.Article;
import cn.javaDataES.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleServiceImpl implements IArticleService {
    @Autowired
    private IArticleDao iArticleDao;

    public void save(Article article) {
        iArticleDao.save(article);
    }

    public void delete(Article article) {
        iArticleDao.delete(article);
    }

    public Iterable<Article> findAll() {
        Iterable<Article> iter = iArticleDao.findAll();
        return iter;
    }

    public Page<Article> findAll(Pageable pageable) {
        return iArticleDao.findAll(pageable);
    }

    public List<Article> findByTitle(String condition) {
        return iArticleDao.findByTitle(condition);
    }
    public Page<Article> findByTitle(String condition, Pageable pageable) {
        return iArticleDao.findByTitle(condition,pageable);
    }
}
