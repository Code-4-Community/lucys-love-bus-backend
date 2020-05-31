# Tests for `service/`

I've written tests for all source files in the service module that belong
to the `dataaccess` and `processor` packages, making use of @connernilsen's
`JooqMock`. Some code in `CheckoutProcessorImplTest.java` is not tested
because it interacts directly with the Stripe API, and is very difficult
to accomplish without a ton of mocking.

Additionally, the tests do not completely handle `null` cases
because that validation is being done before service methods
are called.

~ Brandon Liang (@54skyxenon)