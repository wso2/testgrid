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

import React, {Component} from 'react';
import '../App.css';
import Subheader from 'material-ui/Subheader';
import {Card, CardHeader, CardMedia} from 'material-ui/Card';
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

class TestRunArtifactView extends Component {

    constructor(props) {
        super(props);
        this.state = {
            testScenarioSummaries: [],
            scenarioTestCaseEntries: [],
            testSummaryLoadStatus: "PENDING",
            logContent: "",
            logDownloadStatus: "PENDING",
            logDownloadLink: "",
            isLogTruncated: false
        };
    }

    componentDidMount() {
        const testScenarioSummaryUrl = '/testgrid/v0.9/api/test-plans/test-summary/' +
            this.props.active.reducer.currentInfra.testPlanId;
        const logTruncatedContentUrl = '/testgrid/v0.9/api/test-plans/log/' +
            this.props.active.reducer.currentInfra.testPlanId + "?truncate=" + true;
        const logAllContentUrl = '/testgrid/v0.9/api/test-plans/log/' +
            this.props.active.reducer.currentInfra.testPlanId + "?truncate=" + false;

        fetch(testScenarioSummaryUrl, {
            mode: 'cors',
            method: "GET",
            headers: {
                'Accept': 'application/json'
            }
        }).then(response => {
            if (!response.ok) {
                this.setState({testSummaryLoadStatus: "ERROR"})
            }
            return response.json();
        }).then(data => this.setState({
            testScenarioSummaries: data.scenarioSummaries,
            scenarioTestCaseEntries: data.scenarioTestCaseEntries,
            testSummaryLoadStatus: "SUCCESS"
        }));

        fetch(logTruncatedContentUrl, {
            mode: 'cors',
            method: "GET",
            headers: {
                'Accept': 'application/json'
            }
        }).then(response => {
            if (!response.ok) {
                this.setState({logDownloadStatus: "ERROR"})
            }
            return response.json();
        }).then(data =>
            this.setState({
                isLogDownloadError: "SUCCESS",
                logContent: data.second,
                logDownloadLink: logAllContentUrl,
                isLogTruncated: data.first
            }));
    }

    render() {
        return (
            <div>
                {/*Sub header*/}
                {(() => {
                    switch (this.props.active.reducer.currentInfra.testPlanStatus) {
                        case "FAIL":
                            return <Subheader style={{
                                fontSize: '20px',
                                backgroundColor: "#ffd6d3"
                            }}>
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style={{padding: 5}}>
                                            <img
                                                src={require('../close.png')}
                                                style={{
                                                    verticalAlign: "middle",
                                                    height: "50px",
                                                    width: "50px"
                                                }}/>
                                        </td>
                                        <td style={{padding: 5}}>
                                            <i> {this.props.active.reducer.currentProduct.productName} {this.props.active.reducer.currentProduct.productVersion}
                                                {this.props.active.reducer.currentProduct.productChannel} / {this.props.active.reducer.currentDeployment.deploymentName} /
                                                {this.props.active.reducer.currentInfra.infraParameters}</i>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </Subheader>;
                        case "SUCCESS":
                            return <Subheader style={{
                                fontSize: '20px',
                                backgroundColor: "#cdffba"
                            }}>
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style={{padding: 5}}>
                                            <img
                                                src={require('../success.png')}
                                                style={{
                                                    verticalAlign: "middle",
                                                    height: "50px",
                                                    width: "50px"
                                                }}/>
                                        </td>
                                        <td style={{padding: 5}}>
                                            <i> {this.props.active.reducer.currentProduct.productName} {this.props.active.reducer.currentProduct.productVersion}
                                                {this.props.active.reducer.currentProduct.productChannel} / {this.props.active.reducer.currentDeployment.deploymentName} /
                                                {this.props.active.reducer.currentInfra.infraParameters}</i>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </Subheader>;
                        case "PENDING":
                        case "RUNNING":
                        default:
                            return <Subheader style={{
                                fontSize: '20px',
                                backgroundColor: "#d7d9ff"
                            }}>
                                <table>
                                    <tbody>
                                    <tr>
                                        <td style={{padding: 5}}>
                                            <CircularProgress size={80} thickness={8}/>
                                        </td>
                                        <td style={{padding: 5}}>
                                            <i> {this.props.active.reducer.currentProduct.productName} {this.props.active.reducer.currentProduct.productVersion}
                                                {this.props.active.reducer.currentProduct.productChannel} / {this.props.active.reducer.currentDeployment.deploymentName} /
                                                {this.props.active.reducer.currentInfra.infraParameters}</i>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </Subheader>;
                    }
                })()}
                <Card>
                    <CardHeader
                        title="Artifacts"
                        titleStyle={{'fontSize': '20px', 'fontWeight': 'bold'}}
                        subtitle="Artifacts generated from the test run"
                        avatar={
                            <Avatar
                                src={require('../artifact.png')}
                                size={70}
                                style={{
                                    borderRadius: 0,
                                    backgroundColor: "#ffffff"
                                }}/>
                        }>
                    </CardHeader>
                    <CardMedia style={{paddingLeft: 30, paddingRight: 30}}>
                        {/*TestGrid artifacts*/}
                        <List>
                            {(() => {
                                switch (this.state.isLogDownloadError) {
                                    case "ERROR":
                                        return <div style={{
                                            padding: 5,
                                            color: "#D8000C",
                                            backgroundColor: "#FFD2D2"
                                        }}>
                                            <br/>
                                            <strong>Oh snap! </strong>
                                            Error occurred when downloading the log file content.
                                        </div>;
                                    case "SUCCESS":
                                        return <ListItem disabled={true}
                                                         leftAvatar={
                                                             <Avatar
                                                                 src={require('../log.png')}
                                                                 size={50}
                                                                 style={{
                                                                     borderRadius: 0,
                                                                     backgroundColor: "#ffffff"
                                                                 }}/>
                                                         }>
                                            <a href={this.state.logDownloadLink}
                                               download='test.log'>
                                                <FlatButton label="test.log"/>
                                            </a>
                                        </ListItem>;
                                    case "PENDING":
                                    default:
                                        return <div>
                                            <br/>
                                            <br/>
                                            <b>Loading test log...</b>
                                            <br/>
                                            <LinearProgress
                                                mode="indeterminate"/>
                                        </div>;
                                }
                            })()}
                        </List>
                        <Divider inset={false}/>
                        {/*Test execution summary*/}
                        <center><h2>Test execution summary</h2></center>
                        {(() => {
                            switch (this.state.testSummaryLoadStatus) {
                                case "ERROR":
                                    return <div style={{
                                        padding: 5,
                                        color: "#D8000C",
                                        backgroundColor: "#FFD2D2"
                                    }}>
                                        <br/>
                                        <strong>Oh snap! </strong>
                                        Error occurred when loading test summaries.
                                    </div>;
                                case "SUCCESS":
                                    return <div>
                                        <Table>
                                            <TableHeader displaySelectAll={false}
                                                         adjustForCheckbox={false}>
                                                <TableRow>
                                                    <TableHeaderColumn
                                                        style={{width: "5%", textAlign: "center"}}/>
                                                    <TableHeaderColumn>
                                                        <h2>Scenario</h2>
                                                    </TableHeaderColumn>
                                                    <TableHeaderColumn
                                                        style={{width: "15%", textAlign: "center"}}>
                                                        <h2>Total Success</h2>
                                                    </TableHeaderColumn>
                                                    <TableHeaderColumn
                                                        style={{width: "15%", textAlign: "center"}}>
                                                        <h2>Total Failed</h2>
                                                    </TableHeaderColumn>
                                                    <TableHeaderColumn
                                                        style={{width: "15%", textAlign: "center"}}>
                                                        <h2>Success Percentage</h2>
                                                    </TableHeaderColumn>
                                                </TableRow>
                                            </TableHeader>
                                            <TableBody displayRowCheckbox={false}
                                                       showRowHover={true}>
                                                {this.state.testScenarioSummaries.map((data, index) => {
                                                    return (<TableRow key={index}>
                                                        <TableRowColumn style={{width: "5%"}}>
                                                            {(() => {
                                                                switch (data.scenarioStatus) {
                                                                    case "SUCCESS":
                                                                        return <div>
                                                                            <img width="36"
                                                                                 height="36"
                                                                                 src={require('../success.png')}/>
                                                                        </div>;
                                                                    case "FAIL":
                                                                        return <div>
                                                                            <img width="36"
                                                                                 height="36"
                                                                                 src={require('../close.png')}/>
                                                                        </div>;
                                                                    case "PENDING":
                                                                    case "RUNNING":
                                                                    default:
                                                                        return <div>
                                                                            <CircularProgress
                                                                                size={40}
                                                                                thickness={8}/>
                                                                        </div>
                                                                }
                                                            })()}
                                                        </TableRowColumn>
                                                        <TableRowColumn style={{
                                                            fontSize: "15px",
                                                            wordWrap: "break-word",
                                                            whiteSpace: "wrap"
                                                        }}>{data.scenarioName}</TableRowColumn>
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
                                                            }}>{parseFloat(data.successPercentage.toFixed(2))}%</TableRowColumn>
                                                    </TableRow>)
                                                })}
                                            </TableBody>
                                        </Table>
                                        <Divider inset={false}/>
                                        <br/>
                                        {/*Detailed Report for failed test cases*/}
                                        {this.state.scenarioTestCaseEntries.length > 0 ?
                                            <center>
                                                <h2>Detailed Report (failed test cases
                                                    only)</h2>
                                            </center> : ""}
                                        {this.state.scenarioTestCaseEntries.map((data, index) => {
                                            return (
                                                <div>
                                                    <center><h3 style={{
                                                        color: "#e46226"
                                                    }}>{data.scenarioName}</h3></center>
                                                    <Table>
                                                        <TableHeader displaySelectAll={false}
                                                                     adjustForCheckbox={false}>
                                                            <TableRow>
                                                                <TableHeaderColumn
                                                                    style={{
                                                                        width: "5%",
                                                                        textAlign: "center"
                                                                    }}/>
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
                                                                            style={{width: "5%"}}>
                                                                            {entry.isTestSuccess ?
                                                                                <img width="36"
                                                                                     height="36"
                                                                                     src={require('../success.png')}/>
                                                                                :
                                                                                <img width="36"
                                                                                     height="36"
                                                                                     src={require('../close.png')}/>}
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
                                                    <br/>
                                                </div>
                                            )
                                        })}
                                    </div>;
                                case "PENDING":
                                default:
                                    return <div>
                                        <br/>
                                        <br/>
                                        <b>Loading test summaries...</b>
                                        <br/>
                                        <LinearProgress mode="indeterminate"/>
                                    </div>;
                            }
                        })()}
                        <Divider inset={false}/>
                        <br/>
                        {/*Test log*/}
                        <center><h2>Test Run Log</h2></center>
                        {(() => {
                            switch (this.state.isLogDownloadError) {
                                case "ERROR":
                                    return <div style={{
                                        padding: 5,
                                        color: "#D8000C",
                                        backgroundColor: "#FFD2D2"
                                    }}>
                                        <br/>
                                        <strong>Oh snap! </strong>
                                        Error occurred when downloading the log file content.
                                    </div>;
                                case "SUCCESS":
                                    return <div>
                                        <code>
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
                                                }}/>
                                        </code>
                                        {this.state.isLogTruncated ?
                                            <div>
                                                <center>
                                                    <FlatButton
                                                        onClick={() => (fetch(this.state.logDownloadLink, {
                                                            mode: 'cors',
                                                            method: "GET",
                                                            headers: {
                                                                'Accept': 'application/json'
                                                            }
                                                        }).then(response => {
                                                            if (!response.ok) {
                                                                this.setState({logDownloadStatus: "ERROR"})
                                                            }
                                                            return response;
                                                        }).then(data => data.text().then(text =>
                                                            this.setState({
                                                                isLogDownloadError: "SUCCESS",
                                                                logContent: text,
                                                                isLogTruncated: false
                                                            }),
                                                        )))}
                                                        label="see more..."
                                                        labelStyle={{
                                                            fontSize: '20px',
                                                            fontWeight: 600
                                                        }}
                                                        style={{
                                                            color: '#0E457C'
                                                        }}/>
                                                </center>
                                            </div>
                                            : ""}
                                    </div>;
                                case "PENDING":
                                default:
                                    return <div>
                                        <br/>
                                        <br/>
                                        <b>Loading test log...</b>
                                        <br/>
                                        <LinearProgress mode="indeterminate"/>
                                    </div>;
                            }
                        })()}
                    </CardMedia>
                    <br/>
                    <br/>
                </Card>
            </div>
        );
    }
}

export default TestRunArtifactView;
