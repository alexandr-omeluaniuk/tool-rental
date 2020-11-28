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
package ss.platform.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ss.platform.api.dao.CoreDAO;
import ss.platform.api.dao.DataModel;
import ss.platform.api.dao.EntitySearchRequest;
import ss.platform.api.dao.EntitySearchResponse;

/**
 * Entity REST controller.
 * @author ss
 */
@RestController
@RequestMapping("/api/entity")
public class EntityRESTController {
    /** Entity service. */
    @Autowired
    private CoreDAO coreDAO;
    /**
     * Search entities.
     * @param entityName entity name.
     * @param request HTTP request.
     * @return search response.
     * @throws Exception error.
     */
    @RequestMapping(value = "/{entity}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public EntitySearchResponse searchEntities(@PathVariable("entity") String entityName,
            HttpServletRequest request) throws Exception {
        Class entityClass = (Class<? extends Serializable>) Class.forName(entityName);
        return coreDAO.searchEntities(entityClass, EntitySearchRequest.createRequest(request));
    }
    /**
     * Get entity by ID.
     * @param entityName entity name.
     * @param id entity ID.
     * @return entity.
     * @throws Exception error.
     */
    @RequestMapping(value = "/{entity}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataModel getEntityById(@PathVariable("entity") String entityName,
            @PathVariable("id") Long id) throws Exception {
        Class entityClass = (Class<? extends Serializable>) Class.forName(entityName);
        return coreDAO.findById(id, entityClass);
    }
    /**
     * Create entity.
     * @param entityName entity name.
     * @param rawData raw data.
     * @return entity with ID.
     * @throws Exception error.
     */
    @RequestMapping(value = "/{entity}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataModel createEntity(@PathVariable("entity") String entityName, @RequestBody Object rawData)
            throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Class entityClass = (Class<? extends Serializable>) Class.forName(entityName);
        DataModel entity = (DataModel) mapper.convertValue(rawData, entityClass);
        return coreDAO.create(entity);
    }
    /**
     * Update entity.
     * @param entityName entity name.
     * @param rawData raw data.
     * @return empty response.
     * @throws Exception error.
     */
    @RequestMapping(value = "/{entity}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public DataModel updateEntity(@PathVariable("entity") String entityName, @RequestBody Object rawData)
            throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Class entityClass = (Class<? extends Serializable>) Class.forName(entityName);
        DataModel entity = (DataModel) mapper.convertValue(rawData, entityClass);
        return coreDAO.update(entity);
    }
    /**
     * Delete entity.
     * @param entityName entity name.
     * @param id entity ID..
     * @return response.
     * @throws Exception error.
     */
    @RequestMapping(value = "/{entity}/{id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RESTResponse deleteEntity(@PathVariable("entity") String entityName, @PathVariable("id") Long id)
            throws Exception {
        Class entityClass = (Class<? extends Serializable>) Class.forName(entityName);
        coreDAO.delete(id, entityClass);
        return new RESTResponse();
    }
}
