package grailstest

import grails.test.Post

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class PostController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        log.debug ">> hello: ${params}"

        params.max = Math.min(max ?: 10, 100)
        def list = null
        Post.withoutHibernateFilters {
            list = Post.list(params)
        }

        respond list, model:[postCount: Post.count()]
    }

    def show(Post post) {
        respond post
    }

    def create() {
        respond new Post(params)
    }

    @Transactional
    def save(Post post) {
        log.error("params: ${params}")

        if (post == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (post.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond post.errors, view:'create'
            return
        }

        post.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'post.label', default: 'Post'), post.id])
                redirect post
            }
            '*' { respond post, [status: CREATED] }
        }
    }

    def edit(Post post) {
        respond post
    }

    @Transactional
    def update(Post post) {
        if (post == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (post.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond post.errors, view:'edit'
            return
        }

        post.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'post.label', default: 'Post'), post.id])
                redirect post
            }
            '*'{ respond post, [status: OK] }
        }
    }

    @Transactional
    def delete(Post post) {

        if (post == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        post.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'post.label', default: 'Post'), post.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'post.label', default: 'Post'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
