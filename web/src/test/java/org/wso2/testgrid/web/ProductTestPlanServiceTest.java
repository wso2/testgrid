///*
// * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package org.wso2.testgrid.web;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
//import org.testng.Assert;
//import org.testng.IObjectFactory;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.ObjectFactory;
//import org.testng.annotations.Test;
//import org.wso2.testgrid.dao.repository.ProductTestPlanRepository;
//import org.wso2.testgrid.dao.uow.WebAppUOW;
//import org.wso2.testgrid.dao.util.DAOUtil;
//import org.wso2.testgrid.web.api.APIUtil;
//import org.wso2.testgrid.web.api.ProductTestPlanService;
//import org.wso2.testgrid.web.bean.Product;
//
//import javax.persistence.EntityManagerFactory;
//import javax.ws.rs.core.Response;
//
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * This class has the unit-tests for the ProductTestPlanService API.
// */
//@PowerMockIgnore("javax.ws.rs.*")
//@SuppressStaticInitializationFor({"org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils",
//        "org.wso2.carbon.context.CarbonContext"})
//@PrepareForTest({APIUtil.class, DAOUtil.class})
//public class ProductTestPlanServiceTest {
//
//    private static final Log log = LogFactory.getLog(ProductTestPlanServiceTest.class);
//    private WebAppUOW webAppUOW;
//    private ProductTestPlanService productTestPlanService;
//
//    @ObjectFactory
//    public IObjectFactory getObjectFactory() {
//        return new org.powermock.modules.testng.PowerMockObjectFactory();
//    }
//
//    @BeforeClass
//    public void init() {
//        log.info("Initializing ProductTestPlanService tests");
//        initMocks(this);
//        WebAppUOW webAppUOW = Mockito.mock(WebAppUOW.class);
//        org.wso2.testgrid.common.Product productTestPlan = Mockito.mock(org.wso2.testgrid.common.
//                Product.class);
//        ProductTestPlanRepository productTestPlanRepository = Mockito.mock(ProductTestPlanRepository.class);
//        EntityManagerFactory entityManagerFactoryMock = Mockito.mock(EntityManagerFactory.class);
//        try {
//            PowerMockito.stub(PowerMockito.method(DAOUtil.class, "getEntityManagerFactory"))
//                .toReturn(entityManagerFactoryMock);
//            PowerMockito.whenNew(WebAppUOW.class).withNoArguments().thenReturn(webAppUOW);
//            PowerMockito.whenNew(ProductTestPlanRepository.class).withAnyArguments().
//                    thenReturn(productTestPlanRepository);
//            Mockito.when(productTestPlanRepository.findByPrimaryKey(Mockito.any(String.class)))
//                    .thenReturn(productTestPlan);
//            Mockito.when(webAppUOW.getProductTestPlanById(Mockito.any(String.class)))
//                    .thenReturn(Mockito.mock(org.wso2.testgrid.common.Product.class));
//        } catch (Exception e) {
//            log.error("eee");
//        }
//        this.productTestPlanService = new ProductTestPlanService();
//    }
//
//    @Test(description = "Testing if the device is enrolled when the device is enrolled.")
//    public void testGetProductTestPlan() {
//        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getProductTestPlanBean"))
//                .toReturn(Product.class);
//        Response response = this.productTestPlanService.getProductTestPlan("1");
//        Assert.assertNotNull(response);
//        Assert.assertEquals(response.getSuccess(), Response.Status.OK.getStatusCode());
//    }
//
//}
