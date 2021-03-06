/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

// AveragePlugIn - computes average of input data, written as two points
//                 at start and end times of data
// EMF
// August 2004
// Copyright 2004 Creare Incorporated

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.ChannelTree.Node;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler; //for argument parsing
import com.rbnb.utility.RBNBProcess; //alternative to System.exit, so
                                       //don't bring down servlet engine

public class AveragePlugIn {
  private String address=new String("localhost:3333"); //server to connect to
  private String sinkName=new String("AveSink"); //sink connection name
  private String pluginName=new String("AveragePlugIn"); //plugin connection name
  private PlugIn plugin=null; //plugin connection
  private Sink sink=null; //sink connection
  private String remoteSource=null; //name of remote source
  private int debug=0;
  private long timeout=60000; //timeout on requests, default 1 minute

  public AveragePlugIn(String[] args) {
    //parse args
    try {
      ArgHandler ah=new ArgHandler(args);
      if (ah.checkFlag('a')) {
        String addressL=ah.getOption('a');
        if (addressL!=null) address=addressL;
      }
      if (ah.checkFlag('s')) {
        String source=ah.getOption('s');
        if (source!=null) remoteSource=source;
        //channel name completion easier if always ends in /
        if (!remoteSource.endsWith("/")) remoteSource=remoteSource+"/";
      }
      if (ah.checkFlag('n')) {
        String name=ah.getOption('n');
        if (name!=null) pluginName=name;
      }
      if (ah.checkFlag('t')) {
        long to=Long.parseLong(ah.getOption('t'));
        if (to>=-1) timeout=to;
      }
        
    } catch (Exception e) {
      System.err.println("AveragePlugIn argument exception "+e.getMessage());
      e.printStackTrace();
      RBNBProcess.exit(0);
    }
    //source must be specified
    if (remoteSource==null) {
      System.err.println("AveragePlugIn: source must be specified with -s flag");
      RBNBProcess.exit(0);
    }
  }

  public static void main(String[] args) {
    (new AveragePlugIn(args)).exec();
  }

  public void exec() {
    try {
      //make connections
      sink=new Sink();
      sink.OpenRBNBConnection(address,sinkName);
      plugin=new PlugIn();
      plugin.OpenRBNBConnection(address,pluginName);

      //loop handling requests - note multithreaded would be better
      //                       - does not support monitor/subscribe yet
      while (true) {
        PlugInChannelMap picm=plugin.Fetch(-1); //block until request arrives
//System.err.println("VSource received request "+picm);

        //map channels to remote source 
        ChannelMap cm=new ChannelMap();
        for (int i=0;i<picm.NumberOfChannels();i++) {
//System.err.println("VSource picm channel "+picm.GetName(i));
          cm.Add(remoteSource+picm.GetName(i));
        }
		String[] folder=picm.GetFolderList();
		for (int i=0;i<folder.length;i++) {
//System.err.println("VSource in folder "+folder[i]);
			cm.AddFolder(remoteSource+folder[i]);
		}

        //request data from remote source
//System.err.println("VSource making request");
        sink.Request(cm,
                     picm.GetRequestStart(),
                     picm.GetRequestDuration(),
                     picm.GetRequestReference());
        //cm=sink.Fetch(-1,cm);
        ChannelMap cm2=sink.Fetch(timeout,null);
//System.err.println("VSource received response "+cm);
        while (cm2.GetIfFetchTimedOut()) {
System.err.println("VSource request timed out, retry with new connection.");
          sink.CloseRBNBConnection();
          sink=new Sink();
          sink.OpenRBNBConnection(address,sinkName);
          sink.Request(cm,
                       picm.GetRequestStart(),
                       picm.GetRequestDuration(),
                       picm.GetRequestReference());
          cm2=sink.Fetch(timeout,null);
System.err.println("VSource received response "+cm);
        }
        cm.Clear();
        cm=cm2;

        //copy data,times,types to answer original request
		folder=cm.GetFolderList();
		for (int i=0;i<folder.length;i++) {
//System.err.println("VSource cm folder "+folder[i]);
			if (folder[i].startsWith(remoteSource)) {
				folder[i]=folder[i].substring(remoteSource.length());
				if (folder[i].length()>0) {
//System.err.println("VSource out folder "+folder[i]);
					int idx=picm.GetIndex(folder[i]);
					if (idx==-1) picm.AddFolder(folder[i]);
				}
			} else {
				//may get folder that is shorter than remote source if source or
				// routed server is gone, so don't complain about it...
				//System.err.println("AveragePlugIn: unexpected remote folder "+folder[i]);
			}
		}
				
        for (int i=0;i<cm.NumberOfChannels();i++) {
          String chan=cm.GetName(i);
//System.err.println("VSource cm chan "+chan);
          if (chan.startsWith(remoteSource)) {

            chan=chan.substring(remoteSource.length());
            int idx=picm.GetIndex(chan);
            if (idx==-1) idx=picm.Add(chan);
    
// average data, to demonstrate data reduction for OHM across ratty satellite link
            //EMF 1/24/03: switch to new PutDataRef call to avoid
            //             byte order ambiguity
			//picm.PutTimeRef(cm,i);
			//picm.PutDataRef(idx,cm,i);
            if (cm.GetType(i)==ChannelMap.TYPE_FLOAT64) {
              double[] data=cm.GetDataAsFloat64(i);
System.err.println("data length "+data+" "+data.length);
              double[] sum=new double[1];
              for (int j=0;j<data.length;j++) sum[0]+=data[j];
System.err.println("average is "+sum[0]);
              sum[0]/=data.length;
System.err.println("average is "+sum[0]);
              picm.PutTime(cm.GetTimeStart(i),cm.GetTimeDuration(i));
              picm.PutDataAsFloat64(idx,sum);
            } else { //just copy data
              picm.PutTimeRef(cm,i);
              picm.PutDataRef(idx,cm,i);
            }
          } else {
            System.err.println("AveragePlugIn: unexpected remote channel "+cm.GetName(i));
          }
        }

        //send response
        plugin.Flush(picm);
//System.err.println("VSource sent response "+picm);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }//end exec method

}//end AveragePlugIn class

