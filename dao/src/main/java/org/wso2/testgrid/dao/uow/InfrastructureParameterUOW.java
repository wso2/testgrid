/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.testgrid.dao.uow;

import org.wso2.testgrid.common.infrastructure.InfrastructureParameter;
import org.wso2.testgrid.common.infrastructure.InfrastructureValueSet;
import org.wso2.testgrid.dao.EntityManagerHelper;
import org.wso2.testgrid.dao.TestGridDAOException;
import org.wso2.testgrid.dao.repository.InfrastructureParameterRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;

import static org.wso2.testgrid.common.infrastructure.InfrastructureParameter.INFRASTRUCTURE_PARAMETER_NAME_COLUMN;
import static org.wso2.testgrid.common.infrastructure.InfrastructureParameter
        .INFRASTRUCTURE_PARAMETER_READY_FOR_TESTGRID_METAMODEL_NAME;
import static org.wso2.testgrid.common.infrastructure.InfrastructureParameter
        .INFRASTRUCTURE_PARAMETER_TYPE_METAMODEL_NAME;
import static org.wso2.testgrid.common.infrastructure.InfrastructureParameter.Type;

/**
 * This class defines the Unit of work related to a {@link InfrastructureParameter}.
 *
 * @since 1.0
 */
public class InfrastructureParameterUOW {

    private final InfrastructureParameterRepository infraParamRepository;

    /**
     * Constructs an instance of {@link InfrastructureParameterUOW} to manager use cases related to product test plan.
     */
    public InfrastructureParameterUOW() {
        EntityManager entityManager = EntityManagerHelper.getEntityManager();
        infraParamRepository = new InfrastructureParameterRepository(entityManager);
    }

    /**
     * Returns an instance of {@link InfrastructureParameter} for the given name.
     *
     * @param name parameter name
     * @return an instance of {@link InfrastructureParameter} for the given name.
     */
    public Optional<InfrastructureParameter> getInfrastructureParameter(String name)
            throws TestGridDAOException {
        // Search criteria parameters
        Map<String, Object> params = new HashMap<>();
        params.put(INFRASTRUCTURE_PARAMETER_NAME_COLUMN, name);

        List<InfrastructureParameter> infraParams = infraParamRepository.findByFields(params);
        if (infraParams.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(infraParams.get(0));
    }

    /**
     * Returns a List of {@link InfrastructureParameter} instances.
     *
     * @return a List of {@link InfrastructureParameter} instances
     */
    public List<InfrastructureParameter> getInfrastructureParameters() throws TestGridDAOException {
        return infraParamRepository.findAll();
    }

    /**
     *
     * @return a set of {@link InfrastructureValueSet} instances. Each set item contains a
     * {@link InfrastructureValueSet} of a given {@link Type}.
     */
    public Set<InfrastructureValueSet> getValueSet() throws TestGridDAOException {
        List<InfrastructureParameter.Type> types = infraParamRepository
                .find(((root, query, cb) -> {
                    query.select(root.get(INFRASTRUCTURE_PARAMETER_TYPE_METAMODEL_NAME)).distinct(true);
                    return cb.isTrue(root.get(INFRASTRUCTURE_PARAMETER_READY_FOR_TESTGRID_METAMODEL_NAME));
                }), Type.class);

        //create sets of ValueSets by infrastructure type
        Set<InfrastructureValueSet> infrastructureParameterSets = new HashSet<>();
        for (Type type : types) {
            List<InfrastructureParameter> infrastructureParameters = infraParamRepository.find((root, query, cb) -> {
                //                query.select(root.get("type")).distinct(true);
                return cb.and(
                        cb.equal(root.get(INFRASTRUCTURE_PARAMETER_TYPE_METAMODEL_NAME), type),
                        cb.isTrue(root.get(INFRASTRUCTURE_PARAMETER_READY_FOR_TESTGRID_METAMODEL_NAME)));
            }, InfrastructureParameter.class);

            InfrastructureValueSet valueSet = new InfrastructureValueSet(type, new TreeSet<>(infrastructureParameters));
            infrastructureParameterSets.add(valueSet);
        }
        return infrastructureParameterSets;
    }

    /**
     * This method persists a {@link InfrastructureParameter} instance to the database.
     *
     * @param infraParam {@link InfrastructureParameter} to persist.
     * @return persisted {@link InfrastructureParameter} instance
     * @throws TestGridDAOException thrown when error on persisting the object
     */
    public InfrastructureParameter persistInfrastructureParameter(InfrastructureParameter infraParam) throws
            TestGridDAOException {
        return infraParamRepository.persist(infraParam);
    }

}
