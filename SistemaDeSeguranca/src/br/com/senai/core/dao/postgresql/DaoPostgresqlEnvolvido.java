package br.com.senai.core.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.senai.core.dao.DaoEnvolvido;
import br.com.senai.core.dao.ManagerDb;
import br.com.senai.core.domain.Envolvido;

public class DaoPostgresqlEnvolvido implements DaoEnvolvido {

	private final String INSERT = "INSERT INTO envolvidos (nome,"
			+ " documento) VALUES (?, ?)";
	private final String UPDATE = "UPDATE envolvidos SET nome = ?,"
			+ " documento = ? WHERE id = ?";
	private final String DELETE = "DELETE FROM envolvidos WHERE id = ?";
	private final String SELECT_BY_ID = "SELECT id, nome, documento"
			+ " FROM envolvidos WHERE id = ?";
	private final String SELECT_BY_NOME = "SELECT id, nome, documento"
			+ " FROM envolvidos WHERE Upper(nome) LIKE Upper(?)";
	private final String SELECT_TODAS = "SELECT en.id, en.nome, en.documento"
			+ " FROM envolvidos AS en"
			+ " ORDER BY en.nome";
	private final String SELECT_ID_EXISTENTE = "SELECT COUNT(ocorrencias.id_envolvido) as qtde "
			+ " FROM ocorrencias"
			+ " WHERE ocorrencias.id_envolvido = ?";
	
	
	private final String SELECT_BY_DESC = "SELECT id, nome, documento FROM envolvidos WHERE documento = ? ";
		
	private Connection conexao;
	
	public DaoPostgresqlEnvolvido() {
		this.conexao = ManagerDb.getInstance().getConexao();
	}

	
	@Override
	public void inserir(Envolvido envolvido) {
		PreparedStatement ps = null;
		try {
			ps = conexao.prepareStatement(INSERT);
			ps.setString(1, envolvido.getNomeCompleto());
			ps.setString(2, envolvido.getDocumento());
			ps.execute();
		} catch (Exception ex) {
			throw new RuntimeException("Ocorreu um erro na inser��o"
					+ " do envolvido. Motivo: " + ex.getMessage());
		} finally {
			ManagerDb.getInstance().fechar(ps);
		}
		
	}

	@Override
	public void alterar(Envolvido envolvido) {
		PreparedStatement ps = null;
		try {
			ManagerDb.getInstance().configurarAutoCommitDa(conexao, false);			
			ps = conexao.prepareStatement(UPDATE);
			ps.setString(1, envolvido.getNomeCompleto());
			ps.setString(2, envolvido.getDocumento());
			ps.setInt(3, envolvido.getId());
			boolean isAlteracaoOK = ps.executeUpdate() == 1;
			if (isAlteracaoOK) {
				this.conexao.commit();
			} else {
				this.conexao.rollback();
			}
			ManagerDb.getInstance().configurarAutoCommitDa(conexao, true);		
		} catch (Exception ex) {
			throw new RuntimeException("Ocorreu um erro ao "
					+ "alterar o envolvido. Motivo: " + ex.getMessage());
		} finally {
			ManagerDb.getInstance().fechar(ps);
		}
		
	}

	@Override
	public void excluirPor(int id) {
		PreparedStatement ps = null;
		try {
			ManagerDb.getInstance().configurarAutoCommitDa(conexao, false);
			ps = conexao.prepareStatement(DELETE);
			ps.setInt(1, id);
			boolean isExclusaoOK = ps.executeUpdate() == 1;
			if (isExclusaoOK) {
				this.conexao.commit();				
			} else {
				this.conexao.rollback();
			}
			ManagerDb.getInstance().configurarAutoCommitDa(conexao, true);
		} catch (SQLException ex) {
			throw new RuntimeException("N�o � poss�vel excluir "
					+ "o envolvido pois ele est� vinculado a uma ocorr�ncia. "
					+ "\n Para um normal funcionamento, reinicie o programa novamente.");
		} 
		catch (Exception ex) {
			throw new RuntimeException("Ocorreu um erro ao "
					+ "excluir o envolvido. Motivo: " + ex.getMessage());
		
		} finally {
			ManagerDb.getInstance().fechar(ps);
		}	
	}

	@Override
	public Envolvido buscarPor(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conexao.prepareStatement(SELECT_BY_ID);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				return extrairDo(rs);
			}
			return null;
		} catch (Exception ex) {
			throw new RuntimeException("Ocorreu um erro ao "
					+ "buscar o envolvido. Motivo: " + ex.getMessage());
		} finally {
			ManagerDb.getInstance().fechar(ps);
			ManagerDb.getInstance().fechar(rs);
		}
		
	}

	@Override
	public List<Envolvido> listarPor(String nomeCompleto) {
		List<Envolvido> envolvidos = new ArrayList<Envolvido>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conexao.prepareStatement(SELECT_BY_NOME);
			ps.setString(1, nomeCompleto);
			rs = ps.executeQuery();
			while (rs.next()) {
				envolvidos.add(extrairDo(rs));
			}
			return envolvidos;
		} catch (Exception ex) {
			throw new RuntimeException("Ocorreu um erro ao "
					+ "listar os envolvidos. Motivo: " + ex.getMessage());
		} finally {
			ManagerDb.getInstance().fechar(ps);
			ManagerDb.getInstance().fechar(rs);
		}
	}
	private Envolvido extrairDo(ResultSet rs) {
		try {
			int id = rs.getInt("id");
			String nomeCompleto = rs.getString("nome");
			String documento = rs.getString("documento");
			return new Envolvido(id, nomeCompleto, documento);
		}catch (Exception ex) {
			throw new RuntimeException("Ocorreu um erro ao "
					+ "extrair o envolvido. Motivo: " + ex.getMessage());
		}
	}
	
	@Override
	public Envolvido buscarPorDocumento(String documento) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conexao.prepareStatement(SELECT_BY_DESC);
			ps.setString(1, documento);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				return extrairDo(rs);
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Ocorreu um erro ao buscar o documento do envolvido. Motivo: " + e.getMessage());
		}
		
	}
	
	public List<Envolvido> listarTodas() {
		List<Envolvido> envolvidos = new ArrayList<Envolvido>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conexao.prepareStatement(SELECT_TODAS);
			rs = ps.executeQuery();
			while (rs.next()) {
				envolvidos.add(extrairDo(rs));
			}
		} catch (Exception ex) {
			throw new RuntimeException("Ocorreu um erro na listagem de envolvidos. Motivo "
					+ ex.getMessage());
		} finally {
			ManagerDb.getInstance().fechar(ps);
			ManagerDb.getInstance().fechar(rs);
		}
		return envolvidos;
	}


	@Override
	public boolean validarRemocao(int id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conexao.prepareStatement(SELECT_ID_EXISTENTE);
			ps.setInt(1, id);
			
			boolean isValidadoOK = false;
			
			rs = ps.executeQuery();
			if (rs.next()) {
				isValidadoOK = rs.getInt("qtde") > 0;
			}

			return isValidadoOK;
						
		} catch (Exception e) {
			throw new RuntimeException("Ocorreu um erro na valida��o "
					+ "do id para remo��o. Motivo: " + e.getMessage());
		} finally {
			ManagerDb.getInstance().fechar(ps);
			ManagerDb.getInstance().fechar(rs);
		}
		
	}

}
