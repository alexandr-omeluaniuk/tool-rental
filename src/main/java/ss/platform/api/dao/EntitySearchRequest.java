/*
 * The MIT License
 *
 * Copyright 2020 ss.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ss.platform.api.dao;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 * Entity search request.
 * @author ss
 */
public class EntitySearchRequest {
    /** Default limit for SQL requests. */
    private static final int DEFAULT_LIMIT = 10000;
    // =========================================== FIELDS =============================================================
    /** Page number. Required. */
    private Integer page;
    /** Page size. Required. */
    private Integer pageSize;
    /** Order, asc, desc. */
    private String order;
    /** Order by field. */
    private String orderBy;
    // =========================================== ACTIONS ============================================================
    /**
     * Create new request.
     * @param request HTTP request.
     * @return new search request.
     */
    public static EntitySearchRequest createRequest(HttpServletRequest request) {
        EntitySearchRequest searchRequest = new EntitySearchRequest();
        Enumeration enumeration = request.getParameterNames();
        while(enumeration.hasMoreElements()){
            String parameterName = enumeration.nextElement().toString();
            String value = request.getParameter(parameterName);
            switch (parameterName) {
                case "page":
                    searchRequest.setPage(Integer.valueOf(value));
                    break;
                case "page_size":
                    searchRequest.setPageSize(Integer.valueOf(value));
                    break;
                case "order":
                    searchRequest.setOrder(value);
                    break;
                case "order_by":
                    searchRequest.setOrderBy(value);
                    break;
                default:
                    break;
            }
        }
        return searchRequest;
    }
    // =========================================== SET & GET ==========================================================
    /**
     * @return the page
     */
    public Integer getPage() {
        return page;
    }
    /**
     * @param page the page to set
     */
    public void setPage(Integer page) {
        this.page = page;
    }
    /**
     * @return the pageSize
     */
    public Integer getPageSize() {
        return pageSize;
    }
    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    /**
     * @return the order
     */
    public String getOrder() {
        return order;
    }
    /**
     * @param order the order to set
     */
    public void setOrder(String order) {
        this.order = order;
    }
    /**
     * @return the orderBy
     */
    public String getOrderBy() {
        return orderBy;
    }
    /**
     * @param orderBy the orderBy to set
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
