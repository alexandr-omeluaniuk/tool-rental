/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.platform.api.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core DAO implementation.
 * @author Alexandr Omeluaniuk
 */
@Repository
class CoreDAOImpl implements CoreDAO {
    /** DataModel manager. */
    @PersistenceContext
    private EntityManager em;
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public <T extends DataModel> T create(final T entity) {
        em.persist(entity);
        return entity;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public <T extends DataModel> T update(final T entity) {
        T updated = em.merge(entity);
        return updated;
    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public <T extends DataModel> T findById(final Serializable id, final Class<T> cl) throws Exception {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(cl);
        Root<T> c = criteria.from(cl);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(c.get(DataModel_.id), id));
        criteria.select(c).where(predicates.toArray(new Predicate[0]));
        return em.find(cl, id);
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public <T extends DataModel> void delete(final Serializable id, final Class<T> cl) throws Exception {
        T entity = findById(id, cl);
        if (entity != null) {
            em.remove(entity);
        }
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public <T extends DataModel> void massDelete(Set<Long> ids, Class<T> cl) {
        if (!ids.isEmpty()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete<T> criteria = cb.createCriteriaDelete(cl);
            Root<T> c = criteria.from(cl);
            criteria.where(c.get(DataModel_.id).in(ids));
            em.createQuery(criteria).executeUpdate();
        }
    }
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public <T extends DataModel> EntitySearchResponse searchEntities(Class<T> cl, EntitySearchRequest searchRequest)
            throws Exception {
        EntitySearchResponse response = new EntitySearchResponse();
        // entities data
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(cl);
        Root<T> c = criteria.from(cl);
        List<Predicate> predicates = createSearchCriteria(cb, c, cl, searchRequest);
        criteria.select(c).where(predicates.toArray(new Predicate[0]));
        Optional.ofNullable(searchRequest.getOrderBy()).ifPresent((orderBy) -> {
            if ("asc".equals(searchRequest.getOrder())) {
                criteria.orderBy(cb.asc(c.get(orderBy)));
            } else {
                criteria.orderBy(cb.desc(c.get(orderBy)));
            }
        });
        List<T> entities = em.createQuery(criteria)
                .setFirstResult((searchRequest.getPage() - 1) * searchRequest.getPageSize())
                .setMaxResults(searchRequest.getPageSize()).getResultList();
        response.setData(entities);
        // entities count
        if (!searchRequest.isIgnoreCount()) {
            CriteriaQuery<Long> criteriaCount = cb.createQuery(Long.class);
            Root<T> cCount = criteriaCount.from(cl);
            Expression<Long> sum = cb.count(cCount);
            predicates = createSearchCriteria(cb, cCount, cl, searchRequest);
            criteriaCount.select(sum).where(predicates.toArray(new Predicate[0]));
            List<Long> maxList = em.createQuery(criteriaCount).getResultList();
            Long count = maxList.iterator().next();
            response.setTotal(count == null ? 0 : Integer.valueOf(String.valueOf(count)));
        }
        return response;
    }
    // =========================================== PRIVATE ============================================================
    private <T extends DataModel> List<Predicate> createSearchCriteria(CriteriaBuilder cb, Root<T> c, Class<T> clazz,
            EntitySearchRequest searchRequest) throws Exception {
        List<Predicate> predicates = new ArrayList<>();
        Optional.ofNullable(searchRequest.getFilter()).ifPresent((filter) -> {
            filter.forEach((f) -> {
                predicates.add(fromFilter(f, cb, c));
            });
        });
        return predicates;
    }
    
    private Predicate fromFilter(EntitySearchRequest.FilterCondition filter, CriteriaBuilder cb, Root c) {
        List<Predicate> innerPredicates = new ArrayList<>();
        filter.getPredicates().forEach((filterPredicate) -> {
            Path path;
            if (filterPredicate.getField().contains(".")) {
                From from = c;
                String[] pathParts = filterPredicate.getField().split("\\.");
                for (int i = 0; i < pathParts.length - 1; i++) {
                    from = c.join(pathParts[i], JoinType.LEFT);
                }
                path = from.get(pathParts[pathParts.length - 1]);
            } else {
                path = c.get(filterPredicate.getField());
            }
            if (JPAComparisonOperator.EQUALS.equals(filterPredicate.getOperator())) {
                innerPredicates.add(cb.equal(path, filterPredicate.getValue()));
            } else if (JPAComparisonOperator.LIKE.equals(filterPredicate.getOperator())) {
                innerPredicates.add(cb.like(path, String.valueOf(filterPredicate.getValue())));
            } else if (JPAComparisonOperator.GREATER_THAN_OR_EQUAL_TO.equals(filterPredicate.getOperator())) {
                innerPredicates.add(cb.greaterThanOrEqualTo(path,
                        (Comparable) filterPredicate.getValue()));
            } else if (JPAComparisonOperator.LESS_THAN_OR_EQUAL_TO.equals(filterPredicate.getOperator())) {
                innerPredicates.add(cb.lessThanOrEqualTo(path,
                        (Comparable) filterPredicate.getValue()));
            }
        });
        Optional.ofNullable(filter.getConditions()).ifPresent((conditions) -> {
            conditions.forEach((cond) -> {
                innerPredicates.add(fromFilter(filter, cb, c));
            });
        });
        if (JPABoolConditionOperator.AND.equals(filter.getOperator())) {
            return cb.and(innerPredicates.toArray(new Predicate[0]));
        } else if (JPABoolConditionOperator.OR.equals(filter.getOperator())) {
            return cb.or(innerPredicates.toArray(new Predicate[0]));
        } else {
            throw new RuntimeException("Boolean operator for filter condition is required!");
        }
    }
}
