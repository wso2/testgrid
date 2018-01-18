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
import { add_current_product } from '../actions/testGridActions.js';
import Moment from 'moment'

class ProductStatusView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: []
    };
  }

  componentDidMount() {
    var url = '/testgrid/v0.9/api/products/product-status'
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

  nevigateToRoute(route, product) {
    this.props.dispatch(add_current_product(product));
    this.props.history.push(route);
  }

  render() {
    const products = this.state.hits.map((product, index) => {
      return (<TableRow key={index}>
        <TableRowColumn> <img src={require('../close.png')} width="40" height="40" /></TableRowColumn>
        <TableRowColumn ><h2 style={{ cursor: 'pointer' }} onClick={() => this.nevigateToRoute("/testgrid/v0.9/deployments/product/" + product.id, {
          productId: product.id,
          productName: product.name,
        })}><i>{product.name}</i></h2></TableRowColumn>
        <TableRowColumn> <SingleRecord value={product.status}
          nevigate={() => this.nevigateToRoute("/testgrid/v0.9/deployments/product/", {
            productId: product.id,
            productName: product.name,
          })} time={Moment(product.lastBuild.modifiedTimestamp).fromNow()}
        /></TableRowColumn>
        <TableRowColumn>
          <h4 onClick={() => this.nevigateToRoute("/testgrid/v0.9/deployments/product/" + product.id, {
            productId: product.id,
            productName: product.name,
          })} style={{ cursor: 'pointer', textDecoration: 'underline' }} >{Moment(product.lastfailed.modifiedTimestamp).fromNow()} </h4>
        </TableRowColumn>
        <TableRowColumn> <img src={require('../play.png')} width="48" height="48" /></TableRowColumn>
        <TableRowColumn ><img src={require('../configure.png')} width="36" height="36" style={{ cursor: 'pointer' }}
          onClick={() => { window.location = 'https://testgrid-live-dev.private.wso2.com/job/wso2is-5.4.0/job/01-single-node-deployment-pattern/configure' }} />
        </TableRowColumn>
      </TableRow>)
    });

    return (
      <Table >
        <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
          <TableRow>
            <TableHeaderColumn><h1>Status</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Job</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Latest Status</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Last Failure</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Execute</h1> </TableHeaderColumn>
            <TableHeaderColumn><h1>Configure</h1> </TableHeaderColumn>
          </TableRow>
        </TableHeader>
        <TableBody displayRowCheckbox={false}>
          {products}
        </TableBody>
      </Table>
    )
  }
}

export default ProductStatusView;