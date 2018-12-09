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
import Moment from 'moment';
import {HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT, TESTGRID_API_CONTEXT} from '../constants.js';
import {Card, CardText, Table} from 'reactstrap';

class InfraCombinationHistory extends Component {

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
          <small className="text-muted" key={key}><b>{key}</b>-{value}  </small>
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
    let url = TESTGRID_API_CONTEXT + "/api/test-plans/history/" + this.props.match.params.testPlanId;
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
        currentInfra = {};
        currentInfra.infraParameters = data[0].infraParams;
        currentInfra.relatedProduct = this.props.match.params.productName;
        currentInfra.relatedDeplymentPattern = this.props.match.params.deploymentPatternName;
        this.setState({hits: data, currentInfra: currentInfra});
      })
      .catch(error => console.error(error));
  }

  render() {
    let {productName, deploymentPatternName} = this.props.match.params;
    return (
      <div>
        <Table responsive fixedHeader={false}>
          <thead displaySelectAll={false} adjustForCheckbox={false}>
          <tr>
            <th>#</th>
            <th>Status</th>
            <th>Date</th>
          </tr>
          </thead>
          <tbody displayRowCheckbox={false}>

          {this.state.hits
            .sort((a, b) => b.createdTimestamp - a.createdTimestamp)
            .map((data, index) => {
              return (<tr key={index}>
                <td>{index + 1}</td>
                <td>
                  <div style={{height: 'inherit', width: '100%', 'max-width': '150px'}}
                              onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                productName + "/" +
                                deploymentPatternName + "/test-plans/"
                                + data.id, {
                                deploymentPatternName:
                                this.state.currentInfra.relatedProduct
                              }, {
                                testPlanId: data.id,
                                infraParameters: data.infraParams,
                                testPlanStatus: data.status
                              })}>
                    <SingleRecord value={data.status}/>
                  </div>
                </td>
                <td>{Moment(data.createdTimestamp).calendar()}</td>
              </tr>)
            })}
          </tbody>
        </Table>
      </div>
    );
  }
}

export default InfraCombinationHistory;
