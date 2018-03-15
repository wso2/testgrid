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
 */

import React, { Component } from 'react';
import '../App.css';
import Subheader from 'material-ui/Subheader';
import { Card, CardMedia } from 'material-ui/Card';
import FlatButton from 'material-ui/FlatButton';
import Avatar from 'material-ui/Avatar';
import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import Divider from 'material-ui/Divider';
import AceEditor from 'react-ace';
import LinearProgress from 'material-ui/LinearProgress';
import CircularProgress from 'material-ui/CircularProgress';
import {
  Table,
  TableBody,
  TableHeader,
  TableHeaderColumn,
  TableRow,
  TableRowColumn,
} from 'material-ui/Table';
import Download from 'downloadjs'
import Websocket from 'react-websocket';
import Snackbar from 'material-ui/Snackbar';
import {FAIL, SUCCESS, ERROR, PENDING, RUNNING, HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT} from '../constants.js';


/**
 * View responsible for displaying test run log and summary information.
 *
 * @since 1.0.0
 */
class TestRunView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      testScenarioSummaries: [],
      scenarioTestCaseEntries: [],
      testSummaryLoadStatus: PENDING,
      logContent: "",
      logDownloadStatus: PENDING,
      isLogTruncated: false,
      inputStreamSize: "",
      showLogDownloadErrorDialog: false
    };
  }

  componentDidMount() {
    const testScenarioSummaryUrl = TESTGRID_CONTEXT + '/api/test-plans/test-summary/' +
      this.props.active.reducer.currentInfra.testPlanId;
    const logTruncatedContentUrl = TESTGRID_CONTEXT + '/api/test-plans/log/' +
      this.props.active.reducer.currentInfra.testPlanId + "?truncate=" + true;

    fetch(testScenarioSummaryUrl, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    })
    .then(this.handleError)
    .then(response => {
      this.setState({
        testSummaryLoadStatus: response.ok ? SUCCESS : ERROR
      });
      return response.json();
    }).then(data => this.setState({
      testScenarioSummaries: data.scenarioSummaries,
      scenarioTestCaseEntries: data.scenarioTestCaseEntries
    }));

    if (this.props.active.reducer.currentInfra.testPlanStatus === FAIL ||
      this.props.active.reducer.currentInfra.testPlanStatus === SUCCESS) {
      fetch(logTruncatedContentUrl, {
        method: "GET",
        credentials: 'same-origin',
        headers: {
          'Accept': 'application/json'
        }
      })
      .then(this.handleError)
      .then(response => {
        this.setState({
          logDownloadStatus: response.ok ? SUCCESS : ERROR
        });
        return response.json();
      }).then(data =>
        this.setState({
          logContent: data.inputStreamContent,
          isLogTruncated: data.truncated,
          inputStreamSize: data.completeInputStreamSize
        }));
    }
  }

  handleLogDownloadErrorDialogClose = () => {
    this.setState({
      showLogDownloadErrorDialog: false,
    });
  };

  handleLiveLogData(data) {
    this.setState({ logContent: this.state.logContent + data });
  }

  handleError(response) {
      if (response.status.toString() === HTTP_UNAUTHORIZED) {
          window.location.replace(LOGIN_URI);
      }
      return response;
  }

  render() {
    const subHeader = (<td style={{ padding: 5 }}>
      <i> {this.props.active.reducer.currentProduct.productName}
        {this.props.active.reducer.currentProduct.productVersion}
        {this.props.active.reducer.currentProduct.productChannel} /
        {this.props.active.reducer.currentDeployment.deploymentPatternName} /
        {this.props.active.reducer.currentInfra.infraParameters}</i>
    </td>);
    const divider = (<Divider inset={false} style={{ borderBottomWidth: 1 }} />);
    const logAllContentUrl = TESTGRID_CONTEXT + '/api/test-plans/log/' +
      this.props.active.reducer.currentInfra.testPlanId + "?truncate=" + false;
    let isFailedTestsTitleAdded = false;

    return (
      <div>
        {/*Sub header*/}
        {(() => {
          switch (this.props.active.reducer.currentInfra.testPlanStatus) {
            case FAIL:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#ffd6d3"
              }}>
                <table>
                  <tbody>
                    <tr>
                      <td style={{ padding: 5 }}>
                        <img
                          src={require('../close.png')}
                          style={{
                            verticalAlign: "middle",
                            height: "50px",
                            width: "50px"
                          }} />
                      </td>
                      {subHeader}
                    </tr>
                  </tbody>
                </table>
              </Subheader>;
            case SUCCESS:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <table>
                  <tbody>
                    <tr>
                      <td style={{ padding: 5 }}>
                        <img
                          src={require('../success.png')}
                          style={{
                            verticalAlign: "middle",
                            height: "50px",
                            width: "50px"
                          }} />
                      </td>
                      {subHeader}
                    </tr>
                  </tbody>
                </table>
              </Subheader>;
            case PENDING:
            case RUNNING:
            default:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#d7d9ff"
              }}>
                <table>
                  <tbody>
                    <tr>
                      <td style={{ padding: 5 }}>
                        <CircularProgress size={80} thickness={8} />
                      </td>
                      {subHeader}
                    </tr>
                  </tbody>
                </table>
              </Subheader>;
          }
        })()}
        <Card style={{ padding: 20 }}>
          <CardMedia>
            {/*TestGrid generated contents*/}
            <List>
              <ListItem disabled={true}
                leftAvatar={
                  <Avatar
                    src={require('../log.png')}
                    size={50}
                    style={{
                      borderRadius: 0,
                      backgroundColor: "#ffffff"
                    }} />
                }>
                <FlatButton label="Download Test Run Log"
                  onClick={() => (fetch(logAllContentUrl, {
                    method: "GET",
                    credentials: 'same-origin',
                    headers: {
                      'Accept': 'application/json'
                    }
                  })
                  .then(this.handleError)
                  .then(response => {
                    this.setState({
                      showLogDownloadErrorDialog: !response.ok
                    });
                    return response.json();
                  }).then(data => {
                    if (!this.state.showLogDownloadErrorDialog) {
                      Download(data.inputStreamContent, "test-run.log", "plain/text");
                    }
                  }
                    ))}
                />
                <Snackbar
                  open={this.state.showLogDownloadErrorDialog}
                  message="Error on downloading log file..."
                  autoHideDuration={4000}
                  onRequestClose={this.handleLogDownloadErrorDialogClose}
                  contentStyle={{
                    fontWeight: 600,
                    fontSize: "15px"
                  }}
                />
              </ListItem>
            </List>
            {divider}
            {/*Scenario execution summary*/}
            <h2>Scenario execution summary</h2>
            {(() => {
              switch (this.state.testSummaryLoadStatus) {
                case ERROR:
                  return <div style={{
                    padding: 5,
                    color: "#D8000C",
                    backgroundColor: "#FFD2D2"
                  }}>
                    <br />
                    <strong>Oh snap! </strong>
                    Error occurred when loading test summaries.
                  </div>;
                case SUCCESS:
                  return <div>
                    <Table>
                      <TableHeader displaySelectAll={false}
                        adjustForCheckbox={false}>
                        <TableRow>
                          <TableHeaderColumn
                            style={{ width: "5%", textAlign: "center" }} />
                          <TableHeaderColumn>
                            <h2>Scenario</h2>
                          </TableHeaderColumn>
                          <TableHeaderColumn
                            style={{ width: "15%", textAlign: "center" }}>
                            <h2>Total Success</h2>
                          </TableHeaderColumn>
                          <TableHeaderColumn
                            style={{ width: "15%", textAlign: "center" }}>
                            <h2>Total Failed</h2>
                          </TableHeaderColumn>
                          <TableHeaderColumn
                            style={{ width: "15%", textAlign: "center" }}>
                            <h2>Success Percentage</h2>
                          </TableHeaderColumn>
                        </TableRow>
                      </TableHeader>
                      <TableBody displayRowCheckbox={false}
                        showRowHover={true}>
                        {this.state.testScenarioSummaries.map((data, index) => {
                          return (<TableRow key={index}>
                            <TableRowColumn style={{ width: "5%" }}>
                              {(() => {
                                switch (data.scenarioStatus) {
                                  case SUCCESS:
                                    return <div>
                                      <img width="36"
                                        height="36"
                                        src={require('../success.png')} />
                                    </div>;
                                  case FAIL:
                                    return <div>
                                      <img width="36"
                                        height="36"
                                        src={require('../close.png')} />
                                    </div>;
                                  case PENDING:
                                      return <div>
                                        <img width="36"
                                             height="36"
                                             src={require('../new.png')} />
                                      </div>;
                                  case "RUNNING":
                                  default:
                                    return <div>
                                      <CircularProgress
                                        size={40}
                                        thickness={8} />
                                    </div>
                                }
                              })()}
                            </TableRowColumn>
                            <TableRowColumn style={{
                              fontSize: "15px",
                              wordWrap: "break-word",
                              whiteSpace: "wrap",
                              textDecoration: "none"
                            }}> <a href={"#" + data.scenarioName}> {data.scenarioName} </a></TableRowColumn>
                            <TableRowColumn
                              style={{
                                width: "15%",
                                textAlign: "center",
                                color: "#189800",
                                fontSize: "20px",
                                wordWrap: "break-word",
                                whiteSpace: "wrap"
                              }}>{data.totalSuccess}</TableRowColumn>
                            <TableRowColumn
                              style={{
                                width: "15%",
                                textAlign: "center",
                                color: "#c12f29",
                                fontSize: "20px",
                                wordWrap: "break-word",
                                whiteSpace: "wrap"
                              }}>{data.totalFail}</TableRowColumn>
                            <TableRowColumn
                              style={{
                                width: "15%",
                                textAlign: "center",
                                fontSize: "20px",
                                wordWrap: "break-word",
                                whiteSpace: "wrap"
                              }}>
                              {
                                isNaN(data.successPercentage) ?
                                  "0.0" :
                                  parseFloat(data.successPercentage).toFixed(2)
                              }%
                            </TableRowColumn>
                          </TableRow>)
                        })}
                      </TableBody>
                    </Table>
                    <br />
                    <br />
                    {divider}
                    {/*Detailed Report for failed test cases*/}
                    {this.state.scenarioTestCaseEntries.map((data, index) => {
                      if (data.testCaseEntries.length > 0) {
                        const failedTestTitleContent = isFailedTestsTitleAdded ?
                          "" : <h2>Failed Tests</h2>;
                        isFailedTestsTitleAdded = true;
                        return (
                          <div>
                            {failedTestTitleContent}
                            <h2 style={{
                              color: "#e46226"
                            }}><a id={data.scenarioName} >{data.scenarioName}</a></h2>
                            <Table>
                              <TableHeader displaySelectAll={false}
                                adjustForCheckbox={false}>
                                <TableRow>
                                  <TableHeaderColumn
                                    style={{
                                      width: "5%",
                                      textAlign: "center"
                                    }} />
                                  <TableHeaderColumn
                                    style={{
                                      width: "30%"
                                    }}>
                                    <h2>Test Case</h2>
                                  </TableHeaderColumn>
                                  <TableHeaderColumn
                                    style={{
                                      width: "65%"
                                    }}>
                                    <h2>Failure Message</h2>
                                  </TableHeaderColumn>
                                </TableRow>
                              </TableHeader>
                              <TableBody displayRowCheckbox={false}
                                showRowHover={true}>
                                {data.testCaseEntries.map((entry, index) => {
                                  return (
                                    <TableRow key={index}>
                                      <TableRowColumn
                                        style={{ width: "5%" }}>
                                        {entry.isTestSuccess ?
                                          <img width="36"
                                            height="36"
                                            src={require('../success.png')} />
                                          :
                                          <img width="36"
                                            height="36"
                                            src={require('../close.png')} />}
                                      </TableRowColumn>
                                      <TableRowColumn style={{
                                        fontSize: "15px",
                                        width: "30%",
                                        wordWrap: "break-word",
                                        whiteSpace: "wrap",
                                      }}>{entry.testCase}</TableRowColumn>
                                      <TableRowColumn style={{
                                        fontSize: "15px",
                                        width: "65%",
                                        wordWrap: "break-word",
                                        whiteSpace: "wrap",
                                        paddingTop: 15,
                                        paddingBottom: 15
                                      }}>
                                        {entry.failureMessage}
                                      </TableRowColumn>
                                    </TableRow>
                                  )
                                })}
                              </TableBody>
                            </Table>
                            <br />
                          </div>)
                      } else {
                        return ("")
                      }
                    })}
                  </div>;
                case PENDING:
                default:
                  return <div>
                    <br />
                    <br />
                    <b>Loading test summaries...</b>
                    <br />
                    <LinearProgress mode="indeterminate" />
                  </div>;
              }
            })()}
            {divider}
            <br />
            {/*Test log*/}
            <h2>Test Run Log</h2>
            {/*Display log from file system*/}
            {(() => {
              switch (this.props.active.reducer.currentInfra.testPlanStatus) {
                case PENDING:
                case "RUNNING":
                  return (<div>
                    <Websocket
                      url={'wss://' + window.location.hostname + TESTGRID_CONTEXT + '/live-log/' +
                        this.props.active.reducer.currentInfra.testPlanId}
                      onMessage={data => {
                        this.handleLiveLogData(data)
                      }} />
                    <AceEditor
                      theme="github"
                      ref="log"
                      fontSize={16}
                      showGutter={true}
                      highlightActiveLine={true}
                      value={this.state.logContent}
                      wrapEnabled={true}
                      readOnly={true}
                      setOptions={{
                        showLineNumbers: true,
                        maxLines: Infinity
                      }}
                      style={{
                        width: this.props.containerWidth
                      }} />
                    <center><CircularProgress size={40} thickness={8} /></center>
                  </div>);
                case FAIL:
                case SUCCESS:
                default: {
                  // Display Log from S3
                  switch (this.state.logDownloadStatus) {
                    case ERROR:
                      return <div style={{
                        padding: 5,
                        color: "#D8000C",
                        backgroundColor: "#FFD2D2"
                      }}>
                        <br />
                        <strong>Oh snap! </strong>
                        Error occurred when downloading the log file content.
                      </div>;
                    case SUCCESS:
                      return <div>
                        <AceEditor
                          theme="github"
                          ref="log"
                          fontSize={16}
                          showGutter={true}
                          highlightActiveLine={true}
                          value={this.state.logContent}
                          wrapEnabled={true}
                          readOnly={true}
                          setOptions={{
                            showLineNumbers: true,
                            maxLines: Infinity
                          }}
                          style={{
                            width: this.props.containerWidth
                          }} />
                        {this.state.isLogTruncated ?
                          <div>
                            <center>
                              <FlatButton
                                onClick={() => (fetch(logAllContentUrl, {
                                  method: "GET",
                                  credentials: 'same-origin',
                                  headers: {
                                    'Accept': 'application/json'
                                  }
                                }).then(this.handleError)
                                  .then(response => {
                                  this.setState({
                                    logDownloadStatus: response.ok ? SUCCESS : ERROR
                                  });
                                  return response;
                                }).then(data => data.json().then(json =>
                                  this.setState({
                                    logContent: json.inputStreamContent,
                                    isLogTruncated: false
                                  }),
                                )))}
                                label={"See More (" + this.state.inputStreamSize + ")"}
                                labelStyle={{
                                  fontSize: '20px',
                                  fontWeight: 600
                                }}
                                style={{
                                  color: '#0E457C'
                                }} />
                            </center>
                          </div>
                          : ""}
                      </div>;
                    case PENDING:
                    default:
                      return <div>
                        <br />
                        <br />
                        <b>Loading test log...</b>
                        <br />
                        <LinearProgress mode="indeterminate" />
                      </div>;
                  }
                }
              }
            })()}
          </CardMedia>
          <br />
          <br />
        </Card>
      </div>
    );
  }
}

export default TestRunView;
