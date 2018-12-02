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

import React, {Component} from 'react';
import '../App.css';
import SingleRecord from './SingleRecord.js';
import {add_current_infra, add_current_deployment} from '../actions/testGridActions.js';
import FlatButton from 'material-ui/FlatButton';
import Divider from 'material-ui/Divider';
import Moment from 'moment';
import {FAIL, SUCCESS, ERROR, PENDING, RUNNING, HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT, TESTGRID_API_CONTEXT,
  DID_NOT_RUN, INCOMPLETE} from '../constants.js';
import {Card, CardText, CardTitle, Col, Row, Table} from 'reactstrap';
import ReactTooltip from 'react-tooltip'

class InfraCombinationView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: [],
      currentInfra: null
    }
  }

  handleError(response) {
    if (response.status.toString() === HTTP_UNAUTHORIZED) {
      window.location.replace(LOGIN_URI);
    } else if (!response.ok) {
      throw Error(response.statusText)
    }
    return response;
  }

  static parseInfraCombination(infraCombination){
    let infraData = JSON.parse(infraCombination);
    return <Card body inverse>
      <CardText>
        {
          Object.entries(infraData).map(([key, value]) => {
            return (
              <small className="text-muted"><b>{key}</b>-{value}  </small>
            )
          })
        }
      </CardText>
    </Card>
  }

  navigateToRoute(route, deployment, testPlan) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.dispatch(add_current_infra(testPlan));
    this.props.history.push(route);
  }

  componentDidMount() {
    let currentUrl = window.location.href.split("/");
    let url = TESTGRID_API_CONTEXT + "/api/test-plans/history/" + currentUrl[currentUrl.length - 2];
    let currentInfra;
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    })
      .then(this.handleError)
      .then(response => {
        return response.json()
      })
      .then(data => {
        let currentUrl = window.location.href.split("/");
        if (this.props.active.reducer.currentInfra && this.props.active.reducer.currentInfra.testPlanId === data[0].id) {
          currentInfra = this.props.active.reducer.currentInfra;
        } else {
          currentInfra = {};
          currentInfra.testPlanId = data[0].id;
          currentInfra.infraParameters = data[0].infraParams;
          currentInfra.testPlanStatus = data[0].status;
          this.props.active.reducer.currentInfra = currentInfra;
        }
        currentInfra.relatedProduct = currentUrl[currentUrl.length - 4];
        currentInfra.relatedDeplymentPattern = currentUrl[currentUrl.length - 3];
        this.setState({hits: data, currentInfra: currentInfra});
      })
      .catch(error => console.error(error));
  }

  render() {
    return (
      <div>
        {this.state && this.state.currentInfra && (() => {
          switch (this.state.currentInfra.testPlanStatus) {
            case FAIL:
              return <Row>
                <Col sm="12">
                  <Card body inverse style={{ backgroundColor: '#e57373', borderColor: '#e57373' }}>
                    <CardTitle><i className="fa fa-exclamation-circle" aria-hidden="true" data-tip="Failed!">
                      <span> {this.state.currentInfra.relatedProduct}</span>
                    </i><ReactTooltip/></CardTitle>
                    <CardText>{this.state.currentInfra.relatedDeplymentPattern}</CardText>
                    {InfraCombinationView.parseInfraCombination(this.state.currentInfra.infraParameters)}
                  </Card>
                </Col>
              </Row>;
            case SUCCESS:
              return <Row>
                <Col sm="12">
                  <Card body inverse color="success">
                    <CardTitle><i className="fa fa-check-circle" aria-hidden="true" data-tip="Success!">
                      <span> {this.state.currentInfra.relatedProduct}</span>
                    </i><ReactTooltip/></CardTitle>
                    <CardText>{this.state.currentInfra.relatedDeplymentPattern}</CardText>
                    {InfraCombinationView.parseInfraCombination(this.state.currentInfra.infraParameters)}
                  </Card>
                </Col>
              </Row>;
            case PENDING:
              return <Row>
                <Col sm="12">
                  <Card body inverse color="info">
                    <CardTitle><i className="fa fa-tasks" aria-hidden="true" data-tip="Pending!">
                      <span> {this.state.currentInfra.relatedProduct}</span>
                    </i><ReactTooltip/></CardTitle>
                    <CardText>{this.state.currentInfra.relatedDeplymentPattern}</CardText>
                    {InfraCombinationView.parseInfraCombination(this.state.currentInfra.infraParameters)}
                  </Card>
                </Col>
              </Row>;
            case DID_NOT_RUN:
              return <Row>
                <Col sm="12">
                  <Card body inverse color="info">
                    <CardTitle><i className="fa fa-ban" aria-hidden="true" data-tip="Did Not Run!">
                      <span> {this.state.currentInfra.relatedProduct}</span>
                    </i><ReactTooltip/></CardTitle>
                    <CardText>{this.state.currentInfra.relatedDeplymentPattern}</CardText>
                    {InfraCombinationView.parseInfraCombination(this.state.currentInfra.infraParameters)}
                  </Card>
                </Col>
              </Row>;
            case INCOMPLETE:
              return <Row>
                <Col sm="12">
                  <Card body inverse color="info">
                    <CardTitle><i className="fa fa-hourglass-half" aria-hidden="true" data-tip="Incomplete!">
                      <span> {this.state.currentInfra.relatedProduct}</span>
                    </i><ReactTooltip/></CardTitle>
                    <CardText>{this.state.currentInfra.relatedDeplymentPattern}</CardText>
                    {InfraCombinationView.parseInfraCombination(this.state.currentInfra.infraParameters)}
                  </Card>
                </Col>
              </Row>;
            case ERROR:
              return <Row>
                <Col sm="12">
                  <Card body inverse style={{ backgroundColor: '#e57373', borderColor: '#e57373' }}>
                    <CardTitle><i className="fa fa-times-circle" aria-hidden="true" data-tip="Error!">
                      <span>{this.state.currentInfra.relatedProduct}</span> </i>
                      <ReactTooltip/></CardTitle>
                    <CardText>{this.state.currentInfra.relatedDeplymentPattern}</CardText>
                    {InfraCombinationView.parseInfraCombination(this.state.currentInfra.infraParameters)}
                  </Card>
                </Col>
              </Row>;
            case RUNNING:
            default:
              return <Row>
                <Col sm="12">
                  <Card body inverse color="info">
                    <CardTitle><i className="fa fa-spinner fa-pulse" data-tip="Running!">
                      <span className="sr-only">Loading...</span></i><ReactTooltip/>
                      <span> {this.state.currentInfra.relatedProduct}</span></CardTitle>
                    <CardText>{this.state.currentInfra.relatedDeplymentPattern}</CardText>
                    {InfraCombinationView.parseInfraCombination(this.state.currentInfra.infraParameters)}
                  </Card>
                </Col>
              </Row>;
          }
        })()}
        <Divider/>
        <Table responsive fixedHeader={false}>
          <thead displaySelectAll={false} adjustForCheckbox={false}>
          <tr>
            <th>#</th>
            <th>Status</th>
            <th>Date</th>
            <th>Duration</th>
          </tr>
          </thead>
          <tbody displayRowCheckbox={false}>

          {this.state.hits
            .sort((a, b) => b.createdTimestamp - a.createdTimestamp)
            .map((data, index) => {
              return (<tr key={index}>
                <td>{index + 1}</td>
                <td>
                  <FlatButton style={{height: 'inherit', width: '100%', 'max-width': '150px'}}
                              onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                this.state.currentInfra.relatedProduct + "/" +
                                this.state.currentInfra.relatedDeplymentPattern + "/test-plans/"
                                + data.id, {
                                deploymentPatternName:
                                this.state.currentInfra.relatedProduct
                              }, {
                                testPlanId: data.id,
                                infraParameters: data.infraParams,
                                testPlanStatus: data.status
                              })}>
                    <SingleRecord value={data.status}/>
                  </FlatButton>
                </td>
                <td>{Moment(data.createdTimestamp).calendar()}</td>
                <td>
                  {(() => {
                    let start = Moment(data.createdTimestamp);
                    let end = Moment(data.modifiedTimestamp);
                    let min = end.diff(start, 'minutes');
                    let hours = Math.floor(min / 60);
                    let minutes = min % 60;
                    if (hours > 0) {
                      return (
                        <p><b> {hours} </b> Hours <b> {minutes} </b> Minutes </p>
                      )
                    } else {
                      return (
                        <p><b> {minutes} </b> Minutes </p>
                      )
                    }
                  })()}
                </td>
              </tr>)
            })}
          </tbody>
        </Table>
      </div>
    );
  }
}

export default InfraCombinationView;
