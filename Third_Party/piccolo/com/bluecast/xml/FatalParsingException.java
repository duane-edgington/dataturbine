/*
 * $Id: FatalParsingException.java,v 1.3 2004/07/08 08:03:04 yuvalo Exp $
 *
 * (C) Copyright 2002-2004 by Yuval Oren. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package com.bluecast.xml;

class FatalParsingException extends org.xml.sax.SAXException {
	FatalParsingException(String msg) { super(msg); }
	FatalParsingException(String msg, Exception ex) { super(msg,ex); }
}
