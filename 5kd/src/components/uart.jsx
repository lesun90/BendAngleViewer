// View for the device UART state.
import React from 'react';
import ipc from 'ipc';
import DeviceView from './deviceview.js';
import info from './information.js';

var counter = 5;
var resetBtnState = 0;
var start = new Date().getTime();
var elapsed = 0;
var timeInterval = 1000;
export default class UART extends React.Component {
  constructor() {
    super();
    // Set internal state.
    this.state = {
      device: null,
      services: [],
      angle: "No Data",
      sensor0: ".",
      sensor1: ".",
      nrSensor:null,
      resetButtonText: "Set Zero"
    };

    this.nPull = 0;
    // Manually bind functions so they have proper context.
    this.send = this.send.bind(this);
    this.uartRx = this.uartRx.bind(this);
    this.print = this.print.bind(this);
    this.reset = this.reset.bind(this);
    this.add = this.add.bind(this);
    this.deleteAll = this.deleteAll.bind(this);
    this.deleteLast = this.deleteLast.bind(this);
    this.tick = this.tick.bind(this);
  }

  appendRx(line) {
    // Add a new line to the rx text area.
    $('#rx').val(line + '\r\n');
  }

  send() {
    let data = $('#tx').val();
    this.appendRx('Sent: ' + data + info.props.deviceInfo);
    ipc.send('uartTx', data);
  }

  add(){
    let time = moment().format('MMM DD YYYY, h:mm:ss a');
    $('#rx').val(time +': ' + this.state.angle + ' degree' + '\r\n' + $('#rx').val() );
    this.nPull++;
    //console.log(this.nPull);
  }

  deleteAll(){
    $('#rx').val(null);
    this.nPull = 0;
  }

  deleteLast(){
    if ($('#rx').val() === null)
    {
      return;
    }
    let content = $('#rx').val().split('\n');
    let newContent="";
    for (var i = 1; i<content.length;i++)
    {
      if (content[i] != "")
      {
        newContent += (content[i] + "\n");
      }
    }
    $('#rx').val(newContent);
    this.nPull = this.nPull - 1;

  }

  print() {
    let content = $('#rx').val().split('\n');
    var doc = new jsPDF();
    doc.setFont("courier");
    doc.setFontSize(40);
    doc.text(40, 20, 'DMI INTERNATIONAL');
    doc.text(20, 28, '--------------------');
    doc.setFontSize(20);
    doc.text(20, 40,'Bend Angles: ');
    var offset = 0;
    var nPage = 1;
    for (var i = 0; i < content.length-1; i++)
    {
      if (i<9)
      {
        doc.text(25, 50+10*i - offset,  '  '+ (i+1) +'. '+ content[i]);
      }
      else
      {
        doc.text(25, 50+10*i - offset,  ' '+ (i+1) +'. '+ content[i]);
      }
      if (i>22*nPage)
      {
        nPage++;
        doc.addPage();
        doc.setFontSize(40);
        doc.text(40, 20, 'DMI INTERNATIONAL');
        doc.text(20, 28, '--------------------');
        doc.setFontSize(20);
        doc.text(20, 40,'Bend Angles: ');
        offset = 10*i+10;
      }

    }
    doc.addPage();
    doc.setFontSize(40);
    doc.text(40, 20, 'DMI INTERNATIONAL');
    doc.text(20, 28, '--------------------');
    doc.setFontSize(20);
    doc.text(20, 40,  'Serial Number  : ' + $('#serialNumber').val());
    doc.text(20, 50,  'Number of pull : ' + this.nPull);
    doc.text(20, 60,  'Pipe length    : ' + $('#pipelength').val());
    doc.text(20, 70,  'Pipe number    : ' + $('#pipeNumber').val());
    doc.text(20, 80,  'Heat number    : ' + $('#heatNumber').val());
    doc.text(20, 90,  'Latitude        : ' + $('#latitude').val());
    doc.text(20, 100, 'Longitude     : ' + $('#longitude').val());
    doc.text(20, 110, 'Chain age      : ' + $('#chainAge').val());
    doc.text(20, 120, 'Ovality        : ' + $('#ovality').val());
    doc.text(20, 130, 'Temperature    : ' + $('#temperature').val());
    doc.text(20, 140, 'Note           : ' + $('#note').val());


    doc.save('data.pdf');

  }

  uartRx(data) {
    if (data === null) {
      return;
    }

    this.buffer += data;
    // Look for a newline in the buffer that signals a complete reading.
    let newLine = this.buffer.indexOf('\n');
    if (newLine === -1) {
      // New line not found, stop processing until more data is received.
      return;
    }
    //console.log(this.buffer);
    // Found a new line, pull it out of the buffer.
    let line = this.buffer.slice(0, newLine);
    this.buffer = null;
    // Now parse the components from the reading.
    let components = line.split(',');
    //console.log(Number(components[1]));
    if (components.length != 4) {
      return;
    }

    //
    let nrSensor = Number(components[1]);
    let sensorID = Number(components[2]);
    let angle = Number(components[3]).toFixed(1);

    if (nrSensor === 2)
    {
      if ((sensorID === 0) && (angle<=90) && (angle >= -90) )
      {
        this.setState({
          device: null,
          services: [],
          angle: angle,
          sensor0: "★",
          sensor1: "☆",
          nrSensor: nrSensor,
        });
      }
      else if ((sensorID === 1)&& (angle<=90) && (angle >= -90) )
      {
        this.setState({
          device: null,
          services: [],
          angle: angle,
          sensor0: "☆",
          sensor1: "★",
          nrSensor: nrSensor,
        });
      }
      else
      {
        this.setState({
          device: null,
          services: [],
          angle: "No Data",
          sensor0: "☆",
          sensor1: "☆",
          nrSensor: nrSensor,
        });
      }

    }
    if (nrSensor === 1)
    {
      if (sensorID === 0)
      {
        this.setState({
          device: null,
          services: [],
          angle: "No Data",
          sensor0: "★",
          sensor1: "☆",
          nrSensor: nrSensor,
        });
      }
      if (sensorID === 1)
      {
        this.setState({
          device: null,
          services: [],
          angle: "No Data",
          sensor0: "☆",
          sensor1: "★",
          nrSensor: nrSensor,
        });
      }
    }
    if (nrSensor === 0)
    {
      this.setState({
        device: null,
        services: [],
        angle: "No Data",
        sensor0: "☆",
        sensor1: "☆",
        nrSensor: nrSensor,
      });
    }
  }

  reset(){
    if (resetBtnState === 0)
    {
      ipc.send('uartTx', 's');//start reset process
      resetBtnState = 1;
      var text = ((5000 - elapsed) / 1000).toFixed(1) +' s - Press to cancel';
      this.setState({
        resetButtonText: text
      });
    }
    else if (resetBtnState === 1)
    {
      ipc.send('uartTx', 'b');//broke reset process
      resetBtnState = 0;
      elapsed = 0;
      this.setState({
        resetButtonText: "Set Zero"
      });
    }
  }

  tick(){
    if ((elapsed >= 5000) && (resetBtnState === 1))
    {
      ipc.send('uartTx', 'f');//finish reset process
      resetBtnState = 0;
      elapsed = 0;
      this.setState({
        resetButtonText: "Set Zero"
      });
    }
    else if (resetBtnState === 1)
    {
      var text = ((5000 - elapsed) / 1000).toFixed(1) +' s - Press to cancel';
      this.setState({
        resetButtonText: text
      });
      elapsed = elapsed + timeInterval;
      console.log(elapsed);
    }

  }

  componentDidMount() {
    // Setup async events that will change state of this component.
    ipc.on('uartRx', this.uartRx);
    this.timer = setInterval(this.tick, timeInterval);
  }

  componentWillUnmount() {
    // Be careful to make sure state changes aren't triggered by turning off listeners.
    ipc.removeListener('uartRx', this.uartRx);
    clearInterval(this.timer);
  }
  render(){
    // Render main UART view.
    return (
      <DeviceView index={this.props.params.index}>
        <div className="row">
          <div className="col-sm-2">
            <ul className="nav nav-pills nav-stacked">
              <li className="active"><a data-toggle="tab" href="#information">Informations</a></li>
              <li><a data-toggle="tab" href="#data">Watch Data</a></li>
            </ul>
          </div>
          <div className="col-sm-10">
            <div className="tab-content">
              <div id="information" className="tab-pane fade in active">
                <form className="form-horizontal" role="form">
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="serialNumber">Serial Number</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="serialNumber" placeholder="Serial Number" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="pipelength">Length of pipe</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="pipelength" placeholder="Length of pipe" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="pipeNumber">Pipe Number</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="pipeNumber" placeholder="Pipe Number" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="heatNumber">Heat Number</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="heatNumber" placeholder="Heat Number" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="latitude">Latitude</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="latitude" placeholder="Latitude" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="longitude">longitude</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="longitude" placeholder="longitude" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="chainAge">Chain Age</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="chainAge" placeholder="Chain Age" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="ovality">Ovality</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="ovality" placeholder="Ovality" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="temperature">Temperature</label>
                    <div className="col-xs-10">
                      <input type="number" className="form-control" id="temperature" placeholder="Temperature" />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="control-label col-xs-2" htmlFor="note">Note</label>
                    <div className="col-xs-10">
                      <input type="text" className="form-control" id="note" placeholder="Note" />
                    </div>
                  </div>
                </form>
              </div>
              <div id="data" className="tab-pane fade">
                <div className="row">
                  <div className="col-sm-2 text-center">
                    <h5></h5>
                  </div>
                  <div className="col-sm-3 text-center" style={{backgroundColor: '#003636'}}>
                    <h5>Sensor 1</h5>
                  </div>
                  <div className="col-sm-3 text-center" style={{backgroundColor: '#BC3200'}}>
                    <h5>ANGLE</h5>
                  </div>
                  <div className="col-sm-3 text-center" style={{backgroundColor: '#003636'}}>
                    <h5>Sensor 2</h5>
                  </div>
                </div>

                <div className="row">
                  <div className="col-sm-2 text-center">
                    <h3>DATA</h3>
                  </div>
                  <div className="col-sm-3 text-center" style={{backgroundColor: '#003636'}}>
                    <h3>{this.state.sensor0}</h3>
                  </div>
                  <div className="col-sm-3 text-center" style={{backgroundColor: '#BC3200'}}>
                    <h3>{this.state.angle}</h3>
                  </div>
                  <div className="col-sm-3 text-center" style={{backgroundColor: '#003636'}}>
                    <h3>{this.state.sensor1}</h3>
                  </div>
                </div>

                <div className="row">
                  <div className="col-sm-2 text-center">
                  </div>
                  <div className="col-sm-3 text-center">
                  </div>
                  <div className="col-sm-3 text-center">
                    <button type='button' className='btn btn-default' onClick={this.reset}>{this.state.resetButtonText}</button>
                  </div>
                  <div className="col-sm-3 text-center">
                  </div>
                </div>

                <h3>Data Log:</h3>
                <form role="form">
                  <div className="form-group">
                    <div className="col-xs-8">
                      <div className='form-group'>
                        <textarea id='rx' className="form-control" rows="8" readOnly style={{'backgroundColor': '#ffffff'}}></textarea>
                      </div>
                    </div>
                    <div className="col-xs-2">
                      <p><button type="button" className="btn btn-default" onClick={this.add}>Add Data</button></p>
                      <p><button type="button" className="btn btn-default" onClick={this.deleteLast}>Remove Last Data</button></p>
                      <p><button type="button" className="btn btn-default" onClick={this.deleteAll}>Remove All Data</button></p>
                      <p><button type="button" className="btn btn-default" onClick={this.print}>Export data</button></p>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </DeviceView>
    );
  }
}
