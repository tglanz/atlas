from click.testing import CliRunner

from atlas_utils.cli import main
from atlas_utils.scenarios.make_seed import MOVEMENTS_FILE


def test_make_seed_command_writes_the_migrations(tmp_path, export_dir):
    result = CliRunner().invoke(main, ["make-seed", str(export_dir), "--out-dir", str(tmp_path)])

    assert result.exit_code == 0, result.output
    assert (tmp_path / MOVEMENTS_FILE).exists()


def test_make_seed_command_reports_an_invalid_export(tmp_path):
    bad = tmp_path / "bad"
    bad.mkdir()
    (bad / "push_up.json").write_text("not json")

    result = CliRunner().invoke(main, ["make-seed", str(bad), "--out-dir", str(tmp_path)])

    assert result.exit_code == 1
    assert "not a valid movement file" in result.output
