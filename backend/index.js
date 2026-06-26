require('dotenv').config();
require('./lib/bootstrapCredentials');

const { createApp } = require('./app');

const PORT = Number(process.env.PORT || 5000);
const app = createApp();

app.listen(PORT, () => {
  console.log(`DevConnect API listening on http://localhost:${PORT}`);
  console.log(`Health: http://localhost:${PORT}/health`);
});
