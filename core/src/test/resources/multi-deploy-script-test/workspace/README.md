# testgrid-sample-workspace
A playground to generate and run test plans.


Download Testgrid dist, and set PATH variable.

```
export TESTGRID_HOME=<path/to/WSO2-TestGrid/>
export PATH=$TESTGRID_HOME:$PATH
```

Clone this repo and CD to directory

```
git clone https://github.com/kasunbg/testgrid-sample-workspace.git
cd testgrid-sample-workspace
```

Generate test-plans and Run them

```
testgrid generate-test-plan -p test-job --file job-config.yaml

testgrid run-testplan --product test-job --file /home/kasun/.testgrid/jobs/ref/test-plans/test-plan-01.yaml -w .
testgrid run-testplan --product test-job --file /home/kasun/.testgrid/jobs/ref/test-plans/test-plan-02.yaml -w .
```

Generate email:

```
testgrid generate-email -p test-job -w ~/.testgrid/jobs/test-job
```

View test plan database entries:

```
mysql -uroot -p testgriddb
```

In the mysql-client console, enter the command:

`select id, infra_parameters, status from test_plan where id like 'test-job%';`

