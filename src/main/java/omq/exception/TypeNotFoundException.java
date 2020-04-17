/*****************************************************************************************
 * EVO : an Event-Based Object Distributed Middleware
 * 2003-2011 AST Research Group  
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *****************************************************************************************/
package omq.exception;

/**
 * TypeNotFoundException is thrown if a type is unknown when generating
 * stubs and skeletons.
 *
 */
public class TypeNotFoundException extends Exception {
  static final long serialVersionUID = 10L;

  public TypeNotFoundException (String msg) {
    super (msg);
  }
}
