"""Prueba el control del despliegue sin modificar Docker ni datos reales."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest


class DeployTests(unittest.TestCase):
    def run_deploy(self, *, healthy=True, fail_up=False, fail_http=False):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / '.env').touch()
            mocks = {
                'docker': '''#!/bin/bash
echo "$ORDERS_DEPLOY_IMAGE $*" >> "$MOCK_LOG"
case "$*" in
  *"ps -q mysql") echo mysql-id ;;
  *"ps -q orders-service") echo orders-id ;;
  *".State.Health.Status"*) echo "$MOCK_HEALTH" ;;
  *".Image"*) echo sha256:previous ;;
  *" up "*)
    if [[ "$ORDERS_DEPLOY_IMAGE" == orders-service:new && "$MOCK_FAIL_UP" == 1 ]]; then exit 1; fi ;;
esac
''',
                'python3': '''#!/bin/bash
if [[ "$ORDERS_DEPLOY_IMAGE" == orders-service:new && "$MOCK_FAIL_HTTP" == 1 ]]; then exit 1; fi
exit 0
''',
                'sleep': '#!/bin/bash\nexit 0\n',
            }
            for name, content in mocks.items():
                executable = root / name
                executable.write_text(content)
                executable.chmod(0o755)
            log = root / 'docker.log'
            env = dict(os.environ, PATH=f'{root}:{os.environ["PATH"]}',
                       ORDERS_ENV_FILE=str(root / '.env'), MOCK_LOG=str(log),
                       MOCK_HEALTH='healthy' if healthy else 'unhealthy',
                       MOCK_FAIL_UP=str(int(fail_up)), MOCK_FAIL_HTTP=str(int(fail_http)))
            script = Path(__file__).resolve().parents[1] / 'deploy-local.sh'
            result = subprocess.run(['bash', str(script), 'orders-service:new'],
                                    env=env, capture_output=True, text=True)
            return result, log.read_text()

    def test_despliega_solo_orders_sin_build_ni_dependencias(self):
        result, log = self.run_deploy()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('up -d --no-deps --no-build --force-recreate orders-service', log)
        self.assertNotIn(' down', log)
        self.assertNotIn('sha256:previous compose', log)

    def test_no_despliega_si_mysql_no_esta_healthy(self):
        result, log = self.run_deploy(healthy=False)
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn(' up ', log)

    def test_fallo_de_compose_restaura_imagen_y_reporta_error(self):
        result, log = self.run_deploy(fail_up=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('sha256:previous compose', log)
        self.assertIn('Imagen anterior restaurada', result.stderr)

    def test_fallo_http_restaura_imagen_y_reporta_error(self):
        result, log = self.run_deploy(fail_http=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('sha256:previous compose', log)
        self.assertIn('Imagen anterior restaurada', result.stderr)


if __name__ == '__main__':
    unittest.main()
