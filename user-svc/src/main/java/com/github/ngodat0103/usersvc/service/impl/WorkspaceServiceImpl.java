package com.github.ngodat0103.usersvc.service.impl;

import com.github.ngodat0103.usersvc.dto.WorkspaceDto;
import com.github.ngodat0103.usersvc.dto.mapper.WorkspaceMapper;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import com.github.ngodat0103.usersvc.persistence.document.Workspace;
import com.github.ngodat0103.usersvc.persistence.repository.UserRepository;
import com.github.ngodat0103.usersvc.persistence.repository.WorkspaceRepository;
import com.github.ngodat0103.usersvc.service.WorkspaceService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.github.ngodat0103.usersvc.exception.Util.createConflictException;

@AllArgsConstructor
@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {
  private WorkspaceRepository workspaceRepository;
  private WorkspaceMapper workspaceMapper;
  private UserRepository userRepository;
  private static final String WORKSPACE_IDX = "workspace_idx";

  @Override
  public Mono<WorkspaceDto> create(WorkspaceDto workspaceDto, String accountId) {
    var newWorkspace = workspaceMapper.toDocument(workspaceDto);
    Set<String> members = new HashSet<>();
    members.add(accountId);
    newWorkspace.setMembers(members);
    return workspaceRepository
        .save(newWorkspace)
        .doOnSubscribe(data -> log.info("Creating a new workspace"))
        .doOnSuccess(
            workspace ->
                updateAccountWorkspace(accountId, workspace)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe())
        .doOnError(DuplicateKeyException.class, e -> handleDuplicateKey(e, workspaceDto.getWorkspaceName()))
        .map(workspaceMapper::toDto);
  }
  private void handleDuplicateKey(DuplicateKeyException duplicateKeyException,String workspaceName){
    if(duplicateKeyException.getMessage().contains(WORKSPACE_IDX)){
      throw createConflictException(log,"workspace", "workspaceName", workspaceName);
    }
  }




  private Mono<Account> updateAccountWorkspace(String accountId, Workspace workspace) {
    return userRepository
        .findById(accountId)
        .doOnSubscribe(data -> log.info("Updating the account.workspaces with id: {}", accountId))
        .map(
            account -> {
              Set<String> workspaces = account.getWorkspaces();
              if (workspaces == null) {
                workspaces = new HashSet<>();
              }
              workspaces.add(workspace.getWorkspaceId());
              account.setWorkspaces(workspaces);
              return account;
            })
        .flatMap(userRepository::save)
        .doOnError(
            throwable -> log.error("Error updating account.workspaces with id: {}", accountId))
        .doOnSuccess(
            data -> log.info("Account.workspaces updated successfully with id: {}", accountId));
  }

  @Override
  public Mono<WorkspaceDto> updatePicture(String id, String pictureUrl) {
    return null;
  }

  @Override
  public Mono<WorkspaceDto> update(WorkspaceDto workspaceDto) {
    return null;
  }

  @Override
  public Mono<Void> delete(String id) {
    return null;
  }

  @Override
  public Mono<WorkspaceDto> get(String id) {
    return null;
  }

  @Override
  public Mono<Set<WorkspaceDto>> getWorkspaces(String accountId) {
    return userRepository
        .findById(accountId)
        .doOnSubscribe(data -> log.info("Getting workspaces for account with id: {}", accountId))
        .map(Account::getWorkspaces)
        .flatMapMany(workspaceRepository::findAllById)
        .map(workspaceMapper::toDto)
        .collect(Collectors.toSet())
        .doOnError(
            throwable -> log.error("Error getting workspaces for account with id: {}", accountId))
        .doOnSuccess(
            data ->
                log.info("Workspaces retrieved successfully for account with id: {}", accountId));
  }
}
