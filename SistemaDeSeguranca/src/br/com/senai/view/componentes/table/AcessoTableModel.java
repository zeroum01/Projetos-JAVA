package br.com.senai.view.componentes.table;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import br.com.senai.core.domain.LogsAcesso;

public class AcessoTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private List<LogsAcesso> acessos;
	
	
	public AcessoTableModel(List<LogsAcesso> acesso) {
		this.acessos = acesso;
	}
	
	@Override
	public int getRowCount() {
		return acessos.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		LogsAcesso acessoDaLinha = acessos.get(rowIndex);
		if (columnIndex == 0) {
			return acessoDaLinha.getId();
		} else if (columnIndex == 1) {
			return acessoDaLinha.getDataHora();
		}
		throw new IllegalArgumentException("�ndice inv�lido.");
	}
	
	public LogsAcesso getPor(int rowIndex) {
		return acessos.get(rowIndex);
	}
	
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return "C�digo";
		} else if (columnIndex == 1) {
			return "Hor�rio";
		}
		throw new IllegalArgumentException("�ndice inv�lido.");
	}

}
