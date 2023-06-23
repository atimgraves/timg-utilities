/*Copyright (c) 2023 Tim Graves.

The Universal Permissive License (UPL), Version 1.0

Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both

(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),

without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.

This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package timgutilities.textio;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a command that can be run in a command loop. The
 * commandName represents the name and the runCmd is the executable code.
 */
public class RunnableCommand {
	private String commandName;
	private RunnableWithException runCmd;

	/**
	 * @return the commandName
	 */
	public String getCommandName() {
		return commandName;
	}

	public RunnableCommand(String commandName, RunnableWithException runCmd) {
		super();
		this.commandName = commandName;
		this.runCmd = runCmd;
	}

	/**
	 * executr the command, if an exception is thrownreturn the details, otherwise
	 * return the string output from the command
	 * 
	 * @return
	 */
	public String runIt() {
		try {
			return runCmd.run();
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Exception executing runCmd", e);
			return "Exception executing runCmd, " + e.getMessage();
		}
	}
}
