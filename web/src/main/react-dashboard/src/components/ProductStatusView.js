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
import {add_current_product} from '../actions/testGridActions.js';
import Moment from 'moment'
import ReactTooltip from 'react-tooltip';
import {HTTP_OK, HTTP_NOT_FOUND, HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT} from '../constants.js';
import {Button, Table, Modal, ModalHeader, ModalBody, ModalFooter} from 'reactstrap';

class ProductStatusView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: [],
      modal: false,
      errorMassage: ""
    };

    this.toggle = this.toggle.bind(this);

  }

  handleError(response) {
    if (response.status.toString() === HTTP_UNAUTHORIZED) {
      window.location.replace(LOGIN_URI);
    } else if (!response.ok) {
      throw Error(response.statusText)
    }
    return response;
  }

  toggle(Message) {
    this.setState({
      modal: !this.state.modal,
      errorMassage: Message
    });
  }

  componentDidMount() {
    var url = TESTGRID_CONTEXT + '/api/products/product-status';
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
      .then(data => this.setState({hits: data}))
      .catch(error => console.error(error));
  }

  navigateToRoute(route, product) {
    this.props.dispatch(add_current_product(product));
    this.props.history.push(route);
  }

  downloadReport(productName) {
    let url = TESTGRID_CONTEXT + '/api/products/reports?product-name=' + productName;
    fetch(url, {
      method: "HEAD",
      credentials: 'same-origin',
    }).then(response => {
        if (response.status === HTTP_NOT_FOUND) {
          let errorMessage = "Unable to locate report in the remote storage, please contact the administrator.";
          this.toggle(errorMessage);
        } else if (response.status !== HTTP_OK) {
          let errorMessage = "Internal server error. Couldn't download the report at the moment, please " +
            "contact the administrator.";
          this.toggle(errorMessage);
        } else if (response.status === HTTP_OK) {
          document.location = url;
        }
      }
    ).catch(error => console.error(error));
  }

  render() {
    const products = this.state.hits.map((product, index) => {
      return (<tr key={index}>
        <td><SingleRecord value={product.productStatus}/></td>
        <th onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" + product.productName, {
          productId: product.productId,
          productName: product.productName,
          productStatus: product.productStatus
        })} scope="row">
          <i style={{cursor: 'pointer'}}>{product.productName}</i>
        </th>
        <td style={{fontSize: '16px'}}>
          {(() => {
            if (product.lastBuild.modifiedTimestamp) {
              return (
                <SingleRecord value={product.lastBuild.status}
                              nevigate={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                product.productName, {
                                productId: product.productId,
                                productName: product.productName,
                                productStatus: product.productStatus
                              })} time={product.lastBuild.modifiedTimestamp}
                />)
            } else {
              return ("No builds yet!");
            }
          })()}
        </td>
        <td style={{fontSize: '16px'}}>
          {(() => {
            if (product.lastfailed.modifiedTimestamp) {
              return (
                <i onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" + product.productName, {
                  productId: product.productId,
                  productName: product.productName,
                  productStatus: product.productStatus
                })} style={{cursor: 'pointer'}}>
                  {Moment(product.lastfailed.modifiedTimestamp).fromNow()}</i>
              );
            } else {
              return ("No failed builds yet!")
            }
          })()}
        </td>
        <td><img src={require('../play.png')} alt="" width="36" height="36" data-tip="Execute job" onClick={() => {
          window.location = '/admin/job/' + product.productName + '/build'
        }}/> <ReactTooltip/></td>
        <td><img src={require('../configure.png')} alt="" width="36" height="36" onClick={() => {
          window.location =
            '/admin/job/' + product.productName + '/configure'
        }} data-tip="Configure job" style={{cursor: 'pointer'}}/>
        </td>
        <td>
          <Button color="success" onClick={this.downloadReport.bind(this, product.productName)}>
            Download
          </Button>
        </td>
      </tr>)
    });

    return (
      <div>
        <Table responsive>
          <thead displaySelectAll={false} adjustForCheckbox={false}>
          <tr>
            <th>Status</th>
            <th>Job</th>
            <th>Latest Build</th>
            <th>Last Failure</th>
            <th>Execute</th>
            <th>Configure</th>
            <th>Report</th>
          </tr>
          </thead>
          <tbody displayRowCheckbox={false}>
          {products}
          </tbody>
        </Table>
        <Modal isOpen={this.state.modal} toggle={this.toggle} className={this.props.className} centered={true}>
          <ModalHeader toggle={() => this.toggle("")}>Error</ModalHeader>
          <ModalBody>
            {this.state.errorMassage}
          </ModalBody>
          <ModalFooter>
            <Button color="danger" onClick={() => this.toggle("")}>OK</Button>{' '}
          </ModalFooter>
        </Modal>
      </div>
    )
  }
}

export default ProductStatusView;
