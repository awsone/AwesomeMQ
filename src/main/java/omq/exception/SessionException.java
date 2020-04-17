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
 * Wrapper exception class to different exceptions
 * It simply stores the incoming exception and provides a method for obtaining the "real" exception.
 *
 */
public class SessionException extends Exception {

	private static final long serialVersionUID = 8873640230153201413L;
	private Exception ex;

  public SessionException (Exception ex) {
    super (ex.getMessage());
    ex.printStackTrace();
    this.ex = ex;
  }

  /**
   * This method returns the exception produced in the notification service.
   * @return Exception The exception occured in the underlying notification service.
   */
  public Exception getException() {
    return ex;
  }
}
