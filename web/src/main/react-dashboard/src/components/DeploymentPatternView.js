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

import React, { Component } from 'react';
import '../App.css';
import {
  Table,
  TableBody,
  TableHeader,
  TableHeaderColumn,
  TableRow,
  TableRowColumn,
} from 'material-ui/Table';
import Subheader from 'material-ui/Subheader';
import SingleRecord from './SingleRecord.js';
import { add_current_deployment } from '../actions/testGridActions.js';

class DeploymentPatternView extends Component {

  constructor(props) {
    super(props)
    this.state = {
      hits: []
    };
  }

  componentDidMount() {
    var url = 'http://ec2-34-238-28-168.compute-1.amazonaws.com:8080/testgrid/v0.9/api/deployment-patterns/recent-test-info?productId='
    +this.props.active.reducer.currentProduct.productId+'&date='+this.props.active.reducer.currentProduct.productDate;

    fetch(url, {
      mode: 'cors',
      method: "GET",
      headers: {
        'Accept': 'application/json'
      }
    }).then(response => {
      console.log(response);
      if(response.ok){
        return response.json();
      }
    })
      .then(data => this.setState({ hits: data }));
  }

  nevigateToRoute(route, deployment) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.history.push(route);
  }

  render() {
    console.log(this.state);
    return (
      <div>
        <Subheader style={{ fontSize: '20px' }} > <i> {this.props.active.reducer.currentProduct.productName+" "} 
           {this.props.active.reducer.currentProduct.productVersion} {this.props.active.reducer.currentProduct.productChannel} </i> </Subheader>
        <Table>
          <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
            <TableRow>
              <TableHeaderColumn><h2>Deployment</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Status</h2></TableHeaderColumn>
            </TableRow>
          </TableHeader>
          <TableBody displayRowCheckbox={false}>
            {this.state.hits.map((data, index) => {
              return (<TableRow key={index}>
                <TableRowColumn>
                  <h4>{data.name}</h4></TableRowColumn>
                <TableRowColumn><SingleRecord value={data.testStatus}
                  nevigate={() => this.nevigateToRoute("/testgrid/v0.9/infrastructures/deployment/" + data.id, {
                    deploymentId: data.id,
                    deploymentName: data.name,
                    deploymentDescription: data.description,
                  })}

                /></TableRowColumn>
              </TableRow>)
            })}
          </TableBody>
        </Table>
      </div>
    );
  }
}

export default DeploymentPatternView;