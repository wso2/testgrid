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

class TestRunArtifactView extends Component {

    constructor(props) {
        super(props);
        this.state = {
            hits: [],
            logContent: "",
            logDownloadStatus: "PENDING",
            logDownloadLink: "",
            isLogTruncated: false
        };
    }

    componentDidMount() {
        const testPlanUrl = '/testgrid/v0.9/api/test-plans/' +
            this.props.active.reducer.currentInfra.testPlanId +
            "?require-test-scenario-info=true";
        const logTruncatedContentUrl = '/testgrid/v0.9/api/test-plans/log/' +
            this.props.active.reducer.currentInfra.testPlanId + "?truncate=" + true;
        const logAllContentUrl = '/testgrid/v0.9/api/test-plans/log/' +
            this.props.active.reducer.currentInfra.testPlanId + "?truncate=" + false;

        fetch(testPlanUrl, {
            mode: 'cors',
            method: "GET",
            headers: {
                'Accept': 'application/json'
            }
        }).then(response => {
            if (response.ok) {
                return response.json();
            }
        }).then(data => this.setState({hits: data}));

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
                                            <h3>Log</h3>
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
