var routes = require('../routes');

module.exports = function(app) {
	app.get('/', routes.home);
	app.post('/login', routes.login);
	app.get('/user/:id', routes.account);
	app.get('/user/:id/bookmarks', routes.searchbookmarks);
	app.put('/bookmark', routes.save.saveBookmark);
	app.delete('/bookmark', routes.removebookmark);
	app.get('/savescreen', routes.save.saveScreen);
	app.post('/user/:id/group', routes.creategroup);
	app.get('/logout', routes.logout);
	app.get('*', routes.notfound);
}