package org.wso2.testgrid.core.command;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.testgrid.common.util.StringUtil;
import org.wso2.testgrid.dao.uow.TestPlanUOW;


import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(StringUtil.class)
@PowerMockIgnore({ "javax.management.*", "javax.script.*", "org.apache.logging.log4j.*", "javax.net.ssl.*" })
public class CleanUpCommandTest extends PowerMockTestCase {

    private static final Logger logger = LoggerFactory.getLogger(CleanUpCommandTest.class);

    @Mock
    private TestPlanUOW testPlanUOW;


    @Test
    public void testCleanup() throws Exception {
        testPlanUOW = mock(TestPlanUOW.class);
        List<String> dataToDelete = new ArrayList<String>();
        dataToDelete.add("TP1");
        String grafanaUrl = "ec2-34-232-211-33.compute-1.amazonaws.com:3000";

        when(testPlanUOW.deleteDatasourcesByAge(10)).thenReturn(dataToDelete);
        CleanUpCommand cleanUpCommand = new CleanUpCommand(0, 10, testPlanUOW, dataToDelete,
                grafanaUrl);

        logger.info("Status of the cleanup : " + cleanUpCommand.getStatus());
        cleanUpCommand.execute();
        Assert.assertEquals(cleanUpCommand.getToDelete().size(),dataToDelete.size());
        Assert.assertEquals(cleanUpCommand.getToDelete().get(0),dataToDelete.get(0));
    }

}
