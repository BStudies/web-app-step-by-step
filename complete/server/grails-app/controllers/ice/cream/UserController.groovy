package ice.cream

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer
import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.transform.CompileStatic
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

//<1>
@CompileStatic
@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class UserController {
    static responseFormats = ['json', 'xml']

    UserService userService
    TokenGenerator tokenGenerator
    AccessTokenJsonRenderer accessTokenJsonRenderer
    SpringSecurityService springSecurityService

    def signup(SignupCommand cmd) {
        //<2>
        if ( userService.existsUserByUsername(cmd.username) ) {
            render status: HttpStatus.UNPROCESSABLE_ENTITY.value(), "duplicate key"
            return
        }

        User user = userService.createUser(cmd.username, cmd.password)

        //<3>
        UserDetails userDetails = new GrailsUser(user.username, user.password, user.enabled, !user.accountExpired,
                !user.passwordExpired, !user.accountLocked, user.authorities as Collection<GrantedAuthority>, user.id)
        AccessToken token = tokenGenerator.generateAccessToken(userDetails)
        render status: HttpStatus.OK.value(), accessTokenJsonRenderer.generateJson(token)
    }
}
