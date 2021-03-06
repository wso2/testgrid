/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import {HTTP_OK, HTTP_NOT_FOUND, HTTP_UNAUTHORIZED, LOGIN_URI,
  TESTGRID_CONTEXT, TESTGRID_API_CONTEXT} from '../constants.js';
import {Button, Table, Modal, ModalHeader, ModalBody, ModalFooter} from 'reactstrap';
import FlatButton from 'material-ui/FlatButton';

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
    var url = TESTGRID_API_CONTEXT + '/api/products/product-status';
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

  navigateToJob(product) {
    this.navigateToRoute(TESTGRID_CONTEXT + "/" + product.productName, {
      productId: product.productId,
      productName: product.productName,
      productStatus: product.productStatus
    })
  }

  navigateToRoute(route, product) {
    this.props.dispatch(add_current_product(product));
    this.props.history.push(route);
  }

  downloadReport(productName) {
    let url = TESTGRID_API_CONTEXT + '/api/products/reports?product-name=' + productName;
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
        <td onClick={() => this.navigateToJob(product)}>
          <SingleRecord value={product.productStatus}
          isRunning={product.running}
          />
        </td>
        <th onClick={() => this.navigateToJob(product)} scope="row  ">
          <i style={{cursor: 'pointer'}}>{product.productName}</i>
        </th>
       {/* Note: revisit last success date find logic
        <td style={{fontSize: '16px'}}>
          {(() => {
            if (product.lastSuccessTimestamp) {
              return (
                <i onClick={() => this.navigateToJob(product)}> {Moment(product.lastSuccessTimestamp).fromNow()}</i>
                )
            } else {
              return ("No Success builds yet!");
            }
          })()}
        </td>*/}
        <td style={{fontSize: '16px'}}>
          {(() => {
            if (product.lastFailureTimestamp) {
              return (
                <Button outline color="danger" size="sm" onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" + product.productName, {
                  productId: product.productId,
                  productName: product.productName,
                  productStatus: product.productStatus
                })} style={{cursor: 'pointer'}}>
                  {Moment(product.lastFailureTimestamp).fromNow()}</Button>
              );
            } else {
              return (<FlatButton disabled>No failed builds yet!</FlatButton>)
            }
          })()}
        </td>
        {/* Note: Commented until the backend coordination is configured.
        <td>
          <Button  outline color="info" size="sm" onClick={() => {
            window.location = '/admin/job/' + product.productName + '/build'
          }}>
            <i className="fa fa-play-circle" aria-hidden="true"> </i>
          </Button>
        </td>
        <td>
          <Button outline color="info" size="sm" onClick={() => {
            window.location = '/admin/job/' + product.productName + '/configure'
          }}>
            <i className="fa fa-cogs" aria-hidden="true"> </i>
          </Button>
        </td>*/}
      </tr>)
    });

    return (
      <div>
        <Table hover responsive>
          <thead displaySelectAll={false} adjustForCheckbox={false}>
          <tr>
            <th>Status</th>
            <th>Job</th>
            {/*<th>Last Success</th>*/}
            <th>Last Failure</th>
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
