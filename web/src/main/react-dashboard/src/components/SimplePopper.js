import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Popper from '@material-ui/core/Popper';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Fade from '@material-ui/core/Fade';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import {FAIL, SUCCESS, ERROR, PENDING, RUNNING, HTTP_NOT_FOUND, HTTP_UNAUTHORIZED, LOGIN_URI,
  TESTGRID_API_CONTEXT, DID_NOT_RUN, INCOMPLETE} from '../constants.js';
import {HTTP_OK} from "../constants";

const styles = theme => ({
  typography: {
    padding: theme.spacing.unit * 2,
  },
});

class SimplePopper extends React.Component {
  state = {
    anchorEl: null,
    open: false,
    files: null
  };

  downloadProductLogs() {
    let url = TESTGRID_API_CONTEXT + '/api/test-plans/archives/list/' + this.props.testPlanId;
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    }).then(response => {
        if (response.status === HTTP_NOT_FOUND) {
          let errorMessage = "Unable to locate results in the remote storage.";
          console.log(errorMessage);
        } else if (response.status !== HTTP_OK) {
          let errorMessage = "Internal server error. Couldn't download the results at the moment.";
          console.log(errorMessage);
        } else if (response.status === HTTP_OK) {
          //console.log(response);
          //return response.content;
        }
        return response;
      })
      .then(response => { return response.json();})
      .then(responseData => {console.log(responseData);
        this.setState({files: responseData});
       })
      .catch(error => console.error(error));
  }

  handleClick = event => {
    const { currentTarget } = event;
    this.setState(state => ({
      anchorEl: currentTarget,
      open: !state.open,
    }));
    {this.downloadProductLogs()}
  };

  render() {
    const { classes } = this.props;
    const { anchorEl, open } = this.state;
    const id = open ? 'simple-popper' : null;

    return (
      <div style={{"padding-top": "10px"}}>
        <Button aria-describedby={id} variant="contained" onClick={this.handleClick}>
          <i className="fa fa-download" aria-hidden="true"> </i>  &nbsp;Download archives
        </Button>
        <Popper id={id} open={open} anchorEl={anchorEl} transition>
          {({ TransitionProps }) => (
            <Fade {...TransitionProps} timeout={350}>
              <Paper>
                <Typography className={classes.typography}>
                  <div>
                    {this.state.files && this.state.files.map((data) => {
                      return <div><Button id ="{data}" onClick={()=>{this.props.requestFile(this.props.testPlanId, data)}}>{data}</Button></div>
                    })
                    }
                  {(() => {
                    if (this.state.files === null) {
                      return(
                        <div>
                          Searching relevant archives in the storage..
                          <br/>
                          <CircularProgress style={{margin: "auto"}} className={classes.progress} />
                        </div>);
                    } else if (this.state.files.length === 0) {
                      return <div> No archives found for this test-plan.</div>
                    }
                  })()}
                  </div>
                </Typography>
              </Paper>
            </Fade>
          )}
        </Popper>
      </div>
    );
  }
}

SimplePopper.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SimplePopper);