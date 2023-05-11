/*
 * RErrorCategory.hpp
 *
 * Copyright (C) 2022 by Posit Software, PBC
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

#ifndef R_R_ERROR_CATEGORY_HPP
#define R_R_ERROR_CATEGORY_HPP

#include <boost/system/error_code.hpp>

namespace rstudio {
namespace r {
namespace errc {

enum errc_t {
   Success = 0,
   RHomeNotFound,
   UnsupportedLocale,
   ExpressionParsingError,
   CodeExecutionError,
   SymbolNotFoundError,
   ListElementNotFoundError,
   UnexpectedDataTypeError,
   NoDataAvailableError,
   AttributeNotFoundError
};

} // namespace errc
} // namespace r
} // namespace rstudio


namespace RSTUDIO_BOOST_NAMESPACE {
namespace system {
template <>
struct is_error_code_enum<rstudio::r::errc::errc_t>
 { static const bool value = true; };
} // namespace system
} // namespace boost



#include <shared_core/Error.hpp>

namespace rstudio {
namespace r {

const boost::system::error_category& rCategory();

namespace errc {

inline boost::system::error_code make_error_code( errc_t e )
{
   return boost::system::error_code( e, rCategory() ); }

inline boost::system::error_condition make_error_condition( errc_t e )
{
   return boost::system::error_condition( e, rCategory() );
}

} // namespace errc


core::Error rCodeExecutionError(const std::string& errMsg, 
                                const core::ErrorLocation& location);
   
bool isCodeExecutionError(const core::Error& error, 
                          std::string* pErrMsg = nullptr);
   
// use the error message generated by R for code execution errors,
// otherwise use error.message()
std::string endUserErrorMessage(const core::Error& error);
   

} // namespace r
} // namespace rstudio


#endif // R_R_ERROR_CATEGORY_HPP

