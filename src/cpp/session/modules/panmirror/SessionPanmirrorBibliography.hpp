/*
 * SessionPanmirrorBibliography.hpp
 *
 * Copyright (C) 2022 by RStudio, Inc.
 *
 * Unless you have received this program directly from Posit Software pursuant
 * to the terms of a commercial license agreement with Posit Software, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#ifndef SESSION_MODULES_PANMIRROR_BIBLIOGRAPHY_HPP
#define SESSION_MODULES_PANMIRROR_BIBLIOGRAPHY_HPP

namespace rstudio {
namespace core {
   class Error;
}
}
 
namespace rstudio {
namespace session {
namespace modules { 
namespace panmirror {
namespace bibliography {

core::Error initialize();

} // namespace bibliography
} // namespace panmirror
} // namespace modules
} // namespace session
} // namespace rstudio

#endif /* SESSION_MODULES_PANMIRROR_BIBLIOGRAPHY_HPP */
