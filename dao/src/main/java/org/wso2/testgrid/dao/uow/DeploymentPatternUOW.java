/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.testgrid.dao.uow;

import org.wso2.testgrid.common.DeploymentPattern;
import org.wso2.testgrid.common.Product;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.DeploymentPatternRepository;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class defines the Unit of work related to a {@link Product}.
 *
 * @since 1.0.0
 */
public class DeploymentPatternUOW {

    private final DeploymentPatternRepository deploymentPatternRepository;

    /**
     * Constructs an instance of {@link DeploymentPatternUOW} to manager use cases related to product test plan.
     */
    public DeploymentPatternUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        deploymentPatternRepository = new DeploymentPatternRepository(entityManager);
    }

    /**
     * Returns an instance of {@link Product} for the given product name and product version.
     *
     * @param name    product name
     * @param product product
     * @return an instance of {@link Product} for the given product name and product version
     */
    public Optional<DeploymentPattern> getDeploymentPattern(Product product, String name)
            throws TestGridDAOException {
        // Search criteria parameters
        Map<String, Object> params = new HashMap<>();
        params.put(DeploymentPattern.NAME_COLUMN, name);
        params.put(DeploymentPattern.PRODUCT_COLUMN, product);

        List<DeploymentPattern> deploymentPatterns = deploymentPatternRepository.findByFields(params);
        if (deploymentPatterns.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(deploymentPatterns.get(0));
    }

    /**
     * This method persists a {@link Product} to the database.
     *
     * @param name    product name
     * @param product product
     * @return persisted {@link Product} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public DeploymentPattern persistDeploymentPattern(Product product, String name) throws TestGridDAOException {
        Optional<DeploymentPattern> deploymentPatternOptional = getDeploymentPattern(product, name);
        if (deploymentPatternOptional.isPresent()) {
            return deploymentPatternOptional.get();
        }

        // Create a new product and persist if the product doesn't exist already.
        DeploymentPattern deploymentPattern = new DeploymentPattern();
        deploymentPattern.setName(name);
        deploymentPattern.setProduct(product);
        return deploymentPatternRepository.persist(deploymentPattern);
    }
}
