/**
 * Manage UI layer components for Internal.
 * <p>
 * Use case processing is achieved through application layer components.
 * It is not assumed that UI layer components directly call the domain layer or
 * that controllers are interdependent on each other.
 * DomainEntity is acceptable by interpreting it as a DTO, but care must be
 * taken not to return more information than necessary and create a security
 * risk.
 */
package sample.controller.admin;
