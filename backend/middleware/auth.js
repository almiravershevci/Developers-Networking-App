// backend/middleware/auth.js
const jwt = require('jsonwebtoken');

module.exports = function(req, res, next) {
    // Marrim token-in nga "header"-i i kërkesës
    const token = req.header('x-auth-token');

    // Nëse nuk ka token, kthejmë gabim
    if (!token) return res.status(401).json({ msg: 'Nuk ka token, aksesi u refuzua' });

    try {
        // Verifikojmë token-in (duhet të jetë i njëjtë me atë që ke përdorur te Login)
        const decoded = jwt.verify(token, 'sekreti_yt');
        req.user = decoded.user; // Këtu ruajmë ID-në e përdoruesit
        next(); // Lejohet kalimi te funksioni tjetër
    } catch (e) {
        res.status(400).json({ msg: 'Token i pavlefshëm' });
    }
};