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
import SingleRecord from './SingleRecord.js';
import {add_current_product} from '../actions/testGridActions.js';

class ProductStatusView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: [{
        testStatuses:[]
      }]
    };
  }

  componentDidMount() {
    var url = '/testgrid/v0.9/api/products/test-status?date=2017-12-20'

    fetch(url, {
      method: "GET",
      headers: {
        'Accept': 'application/json'
      }
    }).then(response => {
    
      return response.json();
    })
      .then(data => this.setState({ hits: data }));
  }

  nevigateToRoute(route,product){
    this.props.dispatch(add_current_product(product) );
    this.props.history.push(route);
  }

  render() {  
    console.log(this.state);
    const dates = this.state.hits[0].testStatuses.map((value,index)=>{
      return <TableHeaderColumn key={index}>{value.date.split(' ')[0]}</TableHeaderColumn>
     });
   
    const ptp = this.state.hits.map((value, index) => {
      return (<TableRow key={index}>
        <TableRowColumn><h2>{value.name}</h2></TableRowColumn>
        <TableRowColumn><h3>{value.version}</h3></TableRowColumn>
        <TableRowColumn><h3>{value.channel}</h3></TableRowColumn>
        <TableRowColumn><h3><a href="http://ec2-34-238-28-168.compute-1.amazonaws.com:8080/blue/organizations/jenkins/wso2is-5.4.0%2F01-single-node-deployment-pattern/detail/01-single-node-deployment-pattern/230/pipeline">
        <img src={require('../log.png')}  width="30" height="30"/></a></h3></TableRowColumn>
        <TableRowColumn><h3><a href="http://ec2-34-238-28-168.compute-1.amazonaws.com:8080/job/wso2is-5.4.0/job/01-single-node-deployment-pattern/HTML_Report/wso2is-5.4.0-LTS.html">
        <img src={require('../report.png')}  width="30" height="30"/></a></h3></TableRowColumn>
        {value.testStatuses.map((data, index2) => {
          return (<TableRowColumn key={index2} > <SingleRecord value={data.status} 
          nevigate = {()=>this.nevigateToRoute("/testgrid/v0.9/deployments/product/" + value.id + "/date/" + data.date,{
            productDate:data.date,
            productId: value.id ,
            productName:value.name,
            productVersion:value.version,
            productChannel:value.channel,
          })}
          /></TableRowColumn>);
        })}
      </TableRow>);
    })

    return (
      <Table>
        <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
          <TableRow>
            <TableHeaderColumn><h1>Product</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Version</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Channel</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Logs</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Report</h1> </TableHeaderColumn>
            {dates}
          </TableRow>
        </TableHeader>
        <TableBody displayRowCheckbox={false}>
          {ptp}
        </TableBody>
      </Table>
    )
  }
}

export default ProductStatusView;