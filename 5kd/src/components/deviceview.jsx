// Main device view with links to various device interactions.
import React from 'react';
import {Link} from 'react-router';
import ipc from 'ipc';


export default class DeviceView extends React.Component {
  constructor() {
    super();
    // Set internal state.
    this.state = {name: null, status: null};
    this.connectStatus = this.connectStatus.bind(this);
  }

  connectStatus(status) {
    this.setState({status: status});
  }

  componentDidMount() {
    // Grab the device & connection state for this connection and set state appropriately.
    let device = ipc.sendSync('getDevice', this.props.index);
    let connectStatus = ipc.sendSync('getConnectStatus');
    this.setState({name: device.name, status: connectStatus});
    // Subscribe to connection status changes.
    ipc.on('connectStatus', this.connectStatus);
  }

  componentWillUnmount() {
    // Be careful to make sure state changes aren't triggered by turning off listeners.
    ipc.removeListener('connectStatus', this.connectStatus);
  }

  render() {
    return (
      <div>
        <div className='row'>
          <div className='col-sm-4'>{this.state.name}</div>
          <div className= {this.state.status !== 'Connected' && this.state.status !== null ? 'col-sm-4 list-group-item-warning' : 'col-sm-4 '}>
            <h7 className='list-group-item-heading'>{this.state.status}</h7>
          </div>
          <div className='col-sm-4'>
            <Link to='scan' className='list-group-item'>Disconnect</Link>
          </div>
        </div>
        <div className='row'>
          <div className='page-header'>
            <h3>{this.props.header}</h3>
          </div>
          {this.props.children}
        </div>
      </div>
    )
  }
}
